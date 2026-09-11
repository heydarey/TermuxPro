# Android SDK 项目级恢复能力验收（2026-09-11）

## 背景

本轮定向 Robolectric 测试失败，原因是前一轮为了恢复共享服务器磁盘空间清理了项目级
`.tooling/android-sdk`，但 `local.properties` 仍指向该目录。清理动作虽然安全，但缺少“一键恢复 SDK”
会导致后续本地验证再次卡住。

## 调整

- 新增 `scripts/ensure-android-sdk.sh`：
  - 默认把 Android SDK 安装到项目级 `.tooling/android-sdk`；
  - 下载 Android command-line tools；
  - 安装 `platforms;android-36`、`build-tools;35.0.0`、`platform-tools` 和 `ndk;29.0.14206865`；
  - 生成未跟踪的 `local.properties`；
  - 默认不安装 emulator/system image，避免共享服务器上的高资源占用；
  - 只有显式 `--with-emulator` 才补 emulator 组件。
- command-line tools 下载增加连接超时、总耗时上限和 `.part` 临时文件，避免官方源无数据时长期挂起或留下坏缓存。
- `scripts/bootstrap-dev-env.sh` 在缺少 `sdkmanager` 时自动委托新脚本。
- `docs/DEVELOPMENT.md` 补充清理生成缓存后的 SDK 恢复路径。
- `scripts/pre-push-smoke.sh` 纳入 `test/android-sdk-bootstrap-test.sh`，防止恢复能力再次退化。

## 安全边界

- 不安装系统级 SDK/JDK，不写 shell 配置，不修改全局 `PATH`。
- 不提交 SDK、缓存、`local.properties` 或许可证文件。
- 不启动本地模拟器；当前远程共享环境 KVM 不可用时仍走 CI/低资源验证。

## 本轮本地恢复结果

当前远程服务器访问 `dl.google.com` 时长时间 0 字节下载，已中断本地 SDK 恢复；脚本随后补充超时失败关闭。
本轮 Android UI/Java 改动以 GitHub Actions 的完整 Android CI 作为权威验证来源，本地只执行脚本静态门禁和
dry-run 映射，不伪造本机 SDK 测试通过。
