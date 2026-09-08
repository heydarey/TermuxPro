# Git 最近提交摘要验收记录（2026-09-08）

## 背景

用户反馈 Git 增值能力不能只停留在“能运行命令”，而要按开发者在手机上的真实决策链路组织信息。虽然
Git 工作台已经支持提交记录列表，但入口位于横向按钮组中，用户进入概览后仍需要主动寻找。

## 本轮改动

- Git 工作台概览页直接展示“最近提交”摘要，默认显示最近 3 条提交。
- 提交摘要展示 `短 hash · 相对时间 · 标题`，保持只读，不执行写操作。
- 提交超过 3 条时提示还有多少条，并引导点击“提交记录”查看完整列表。
- 无提交仓库显示空状态，不报错、不误导用户执行额外命令。

## 验收要点

- 不新增远端 Git 写操作，复用已有 `git log -20` 协议输出。
- 用户无需打开二级页面即可判断当前分支最近发生了什么。
- 360dp 宽度下摘要在纵向概览区展示，避免继续塞进横向按钮组。

## 本地验证

- `git diff --check`
- `./scripts/validate-skills.sh`
- `./test/dialog-readable-style-test.sh`
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew :app:testDebugUnitTest --tests com.termux.app.GitDiffActivityTest --tests com.termux.app.GitRepositoryOverviewTest --max-workers=2`
- `TERMUXPRO_USE_CHINA_MIRROR=1 ./scripts/pre-push-smoke.sh origin/dev`
