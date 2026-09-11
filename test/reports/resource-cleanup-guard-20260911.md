# 资源清理守卫报告（2026-09-11）

## 背景

本轮恢复日常迭代时，资源守卫发现工作区磁盘空间不足，主要占用来自可再生成的 Android/Gradle 构建目录、
项目内 Android SDK/JDK 下载缓存，以及用户级 npm/Gradle 缓存。该问题会阻断后续 CI 前置验证，也容易
诱导临时手工清理。

## 治理策略

- 新增 `scripts/cleanup-generated-caches.sh`，默认只做 dry-run，先列出可清理目标和磁盘状态。
- 只有显式传入 `--apply` 才会删除；删除目标必须命中白名单。
- 默认仅处理项目内可再生成目录；用户级 npm/Gradle 缓存必须额外传入 `--include-user-caches`。
- 清理实现禁止 `rm -rf`，使用白名单校验后对目标目录执行 `find -mindepth 1 -delete` 并尝试移除空目录。
- `scripts/resource-guard.sh` 在磁盘不足时提示先运行清理脚本，避免重复手工排查。

## 安全边界

- 不清理 `.vscode-server`、`.codex`、源码、文档、证书、签名配置或真实 tmux 会话。
- 不绕过资源守卫；清理只是释放可再生成缓存，后续仍需重新执行资源检查。
- 不启动本地模拟器；当前共享服务器 KVM 不可用时继续走低资源验证路径。

## 验收

- `./test/generated-cache-cleanup-test.sh` 覆盖脚本可执行、默认 dry-run、白名单、安全删除实现和资源守卫提示。
- `./scripts/pre-push-smoke.sh` 已纳入该测试，后续脚本/文档类变更也会检查清理能力是否退化。
