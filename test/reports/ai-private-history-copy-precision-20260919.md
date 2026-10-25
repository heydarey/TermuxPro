# AI CLI 私有历史文案精确性复核

## 背景

负责人继续走查 AI CLI 会话中心时发现，上一轮已经把“远端 AI 历史 / remote AI history”收敛为
“Claude/Codex 远端历史”，但页面摘要和本地记录提示仍保留“AI 私有历史 / private AI history”的泛化说法。

这类文案不会直接造成功能错误，但在共享 Claude 账号和 Codex 用户级隔离并存的真实场景下，容易让用户误以为
TermuxPro 存在一个统一的 AI 历史库，削弱“只管理本机本 App 启动入口记录”的安全边界。

## 本轮处理

- 将中文 AI 会话中心摘要改为“TermuxPro 不读取 Claude/Codex 私有历史”。
- 将中文本地启动记录提示改为“不读取 Claude/Codex 私有历史或终端输出”。
- 将英文摘要改为 “Claude/Codex private history”。
- 扩展 `test/ai-launch-decision-copy-test.sh`，禁止文案回退到“AI 私有历史”或 `private AI history`。

## 主动发现记录

1. 观察：摘要层仍有泛化“AI 私有历史”。
   - 影响旅程：用户在启动 Claude/Codex 前判断 TermuxPro 是否读取远端历史。
   - 分类：AI CLI、移动端交互体验。
   - 处理：本轮已修复并加防回退测试。
2. 观察：记录区提示“不读取私有历史”缺少主体。
   - 影响旅程：用户清理本地记录时可能误解为清理远端 AI 历史。
   - 分类：AI CLI、本地记录安全边界。
   - 处理：本轮已明确为 Claude/Codex 私有历史。
3. 观察：英文资源也可能在系统语言切换或截图验收中暴露泛化说法。
   - 影响旅程：非中文环境下的安全解释一致性。
   - 分类：AI CLI、国际化体验。
   - 处理：本轮已同步英文并增加静态门禁。

## 验收重点

- 页面说明必须保持“Claude/Codex”具名，避免重新泛化为“AI 历史”。
- 本轮不读取 Claude/Codex 私有历史、不启动远端命令、不进入 tmux、不影响 Termux 原始终端能力。
