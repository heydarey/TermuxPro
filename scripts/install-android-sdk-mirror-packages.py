#!/usr/bin/env python3
"""从 Android SDK 镜像安装项目级 SDK 组件。

这个脚本只写入传入的 --sdk-root，避免因为 dl.google.com 不可达而反复手工安装
系统级 SDK。它按 SDK 包内的 source.properties 校验 Pkg.Path，确保移动目录
时不会把错误包安装到目标路径。
"""

from __future__ import annotations

import argparse
import hashlib
import os
import shutil
import subprocess
import sys
import tempfile
import urllib.request
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path


DEFAULT_REPOSITORY = "repository2-1.xml"


def package_destination(sdk_root: Path, package_path: str) -> Path:
    if package_path.startswith("platforms;"):
        return sdk_root / "platforms" / package_path.split(";", 1)[1]
    if package_path.startswith("build-tools;"):
        return sdk_root / "build-tools" / package_path.split(";", 1)[1]
    if package_path.startswith("ndk;"):
        return sdk_root / "ndk" / package_path.split(";", 1)[1]
    if package_path == "platform-tools":
        return sdk_root / "platform-tools"
    if package_path == "emulator":
        return sdk_root / "emulator"
    raise SystemExit(f"暂不支持从镜像安装 SDK 包：{package_path}")


def read_pkg_path(source_properties: Path) -> str | None:
    for line in source_properties.read_text(encoding="utf-8", errors="replace").splitlines():
        if line.startswith("Pkg.Path="):
            return line.split("=", 1)[1].strip()
    return None


def read_properties(source_properties: Path) -> dict[str, str]:
    properties: dict[str, str] = {}
    for line in source_properties.read_text(encoding="utf-8", errors="replace").splitlines():
        if "=" not in line or line.startswith("#"):
            continue
        key, value = line.split("=", 1)
        properties[key.strip()] = value.strip()
    return properties


def matches_package(source_properties: Path, package_path: str) -> bool:
    properties = read_properties(source_properties)
    if properties.get("Pkg.Path") == package_path:
        return True
    if package_path.startswith("platforms;android-"):
        api_level = package_path.rsplit("-", 1)[1]
        return properties.get("AndroidVersion.ApiLevel") == api_level
    if package_path.startswith("build-tools;"):
        return properties.get("Pkg.Revision") == package_path.split(";", 1)[1]
    if package_path.startswith("ndk;"):
        return properties.get("Pkg.Revision") == package_path.split(";", 1)[1]
    if package_path in ("platform-tools", "emulator"):
        return source_properties.parent.name == package_path
    return False


def download(url: str, target: Path, expected_size: int | None = None) -> None:
    target.parent.mkdir(parents=True, exist_ok=True)
    partial = target.with_suffix(target.suffix + ".part")
    if target.exists() and (expected_size is None or target.stat().st_size == expected_size):
        return
    target.unlink(missing_ok=True)
    if shutil.which("curl"):
        for attempt in range(1, 4):
            print(f"下载 {url}（curl 第 {attempt}/3 次）", flush=True)
            command = [
                "curl",
                "--fail",
                "--location",
                "--retry",
                "5",
                "--retry-delay",
                "2",
                "--connect-timeout",
                "20",
                "--max-time",
                "900",
                "--continue-at",
                "-",
                "--output",
                str(partial),
                url,
            ]
            result = subprocess.run(command, check=False)
            if result.returncode != 0:
                continue
            if expected_size is not None and partial.stat().st_size != expected_size:
                print(
                    f"下载大小不匹配：期望 {expected_size}，实际 {partial.stat().st_size}",
                    file=sys.stderr,
                    flush=True,
                )
                continue
            partial.replace(target)
            return
        partial.unlink(missing_ok=True)
        raise OSError(f"curl 下载失败：{url}")
    for attempt in range(1, 4):
        print(f"下载 {url}（第 {attempt}/3 次）", flush=True)
        bytes_written = 0
        try:
            with urllib.request.urlopen(url, timeout=120) as response, partial.open("wb") as out:
                while True:
                    chunk = response.read(1024 * 1024)
                    if not chunk:
                        break
                    out.write(chunk)
                    bytes_written += len(chunk)
            if expected_size is not None and bytes_written != expected_size:
                raise OSError(f"下载大小不匹配：期望 {expected_size}，实际 {bytes_written}")
            partial.replace(target)
            return
        except Exception:
            partial.unlink(missing_ok=True)
            if attempt == 3:
                raise


def sha1(path: Path) -> str:
    digest = hashlib.sha1()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def load_repository(mirror_base: str, downloads_dir: Path) -> ET.Element:
    repository = downloads_dir / DEFAULT_REPOSITORY
    download(f"{mirror_base}/{DEFAULT_REPOSITORY}", repository)
    return ET.parse(repository).getroot()


def find_archive(root: ET.Element, package_path: str) -> tuple[str, str | None, int | None]:
    candidates = root.findall(f".//remotePackage[@path='{package_path}']")
    if not candidates:
        raise SystemExit(f"镜像索引中找不到 SDK 包：{package_path}")

    def revision_key(package: ET.Element) -> tuple[int, int, int]:
        revision = package.find("revision")
        values: list[int] = []
        for name in ("major", "minor", "micro"):
            node = revision.find(name) if revision is not None else None
            values.append(int(node.text) if node is not None and node.text else 0)
        return tuple(values)  # type: ignore[return-value]

    for package in sorted(candidates, key=revision_key, reverse=True):
        for archive in package.findall("./archives/archive"):
            host = archive.findtext("host-os")
            if host not in (None, "linux"):
                continue
            url = archive.findtext("./complete/url")
            if not url:
                continue
            checksum = archive.findtext("./complete/checksum")
            size_text = archive.findtext("./complete/size")
            size = int(size_text) if size_text else None
            return url, checksum, size
    raise SystemExit(f"镜像索引中找不到 Linux 可用归档：{package_path}")


def install_package(
    *,
    sdk_root: Path,
    downloads_dir: Path,
    mirror_base: str,
    package_path: str,
    archive_url: str,
    expected_sha1: str | None,
    expected_size: int | None,
) -> None:
    destination = package_destination(sdk_root, package_path)
    if (destination / "source.properties").exists() and matches_package(
        destination / "source.properties", package_path
    ):
        print(f"已存在 {package_path}：{destination}", flush=True)
        return

    archive_name = archive_url.rsplit("/", 1)[-1]
    archive_path = downloads_dir / archive_name
    download(f"{mirror_base}/{archive_url}", archive_path, expected_size)
    if expected_sha1:
        actual_sha1 = sha1(archive_path)
        if actual_sha1.lower() != expected_sha1.lower():
            raise SystemExit(
                f"{archive_name} SHA-1 不匹配：期望 {expected_sha1}，实际 {actual_sha1}"
            )

    with tempfile.TemporaryDirectory(prefix="android-sdk.", dir=sdk_root.parent) as temp_name:
        temp_dir = Path(temp_name)
        print(f"解压 {archive_name}", flush=True)
        with zipfile.ZipFile(archive_path) as zip_file:
            zip_file.extractall(temp_dir)

        source_dir = None
        for source_properties in temp_dir.rglob("source.properties"):
            if matches_package(source_properties, package_path):
                source_dir = source_properties.parent
                break
        if source_dir is None:
            raise SystemExit(f"{archive_name} 中没有找到 Pkg.Path={package_path}")

        destination.parent.mkdir(parents=True, exist_ok=True)
        old_destination = destination.with_name(destination.name + ".old")
        if old_destination.exists():
            shutil.rmtree(old_destination)
        if destination.exists():
            destination.replace(old_destination)
        shutil.move(str(source_dir), str(destination))
        if old_destination.exists():
            shutil.rmtree(old_destination)
        print(f"安装完成 {package_path}：{destination}", flush=True)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--sdk-root", required=True)
    parser.add_argument("--mirror-base", required=True)
    parser.add_argument("packages", nargs="+")
    args = parser.parse_args()

    sdk_root = Path(args.sdk_root).resolve()
    mirror_base = args.mirror_base.rstrip("/")
    downloads_dir = sdk_root.parent / "downloads"
    downloads_dir.mkdir(parents=True, exist_ok=True)
    root = load_repository(mirror_base, downloads_dir)

    for package_path in args.packages:
        url, checksum, size = find_archive(root, package_path)
        install_package(
            sdk_root=sdk_root,
            downloads_dir=downloads_dir,
            mirror_base=mirror_base,
            package_path=package_path,
            archive_url=url,
            expected_sha1=checksum,
            expected_size=size,
        )

    local_properties = sdk_root.parent.parent / "local.properties"
    local_properties.write_text(f"sdk.dir={sdk_root}\n", encoding="utf-8")
    return 0


if __name__ == "__main__":
    sys.exit(main())
