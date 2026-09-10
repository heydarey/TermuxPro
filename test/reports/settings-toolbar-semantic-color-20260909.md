# 设置页顶部危险色防回退验收记录

## 增值服务准入

- 分类：移动端工作流效率、UI/UX 信任感。
- 结论：通过。该问题不是重做 Termux 原始终端能力，而是修复 TermuxPro 可视化页面继承上游红色主题后
  带来的危险语义误导。

## 用户问题

用户反馈设置页顶部红色看起来像危险状态。此前已把 `SettingsActivity` Manifest 主题改为
`Theme.TermuxPro.DayNight.NoActionBar`，但回归测试主要检查 Manifest 字符串，不能证明运行时 toolbar
实际颜色不会再次继承上游红色。

## 本轮主动发现

| 观察 | 来源 | 影响旅程 | 优先级 | 下一动作 |
|---|---|---|---|---|
| 共享 toolbar 背景取 `?attr/colorPrimaryDark`，而上游共享主题默认是红色。 | 负责人代码走查 | 设置页、报告页、文本展示页的视觉信任感 | P1 | 对 TermuxPro 产品主题显式覆盖 `colorPrimaryDark` |
| 既有测试只断言 Manifest 文本，没有启动设置页验证真实 toolbar 背景。 | QA 门禁走查 | 后续回归可能“声明没变但运行时颜色回退” | P1 | 增加 Robolectric 运行时主题断言 |
| 设置页属于普通配置入口，不应使用危险色；危险色只应留给删除、停止、错误等状态。 | UI/UX 语义走查 | 用户误判页面风险，降低操作信任 | P1 | 将设置页顶部锁定为 `tp_surface` 中性色 |

## 本轮修复

- `Theme.TermuxPro.DayNight.NoActionBar` 在日间与夜间资源中显式声明
  `colorPrimaryDark=@color/tp_surface`，不再依赖上游红色默认链路。
- `ManifestProductIdentityTest` 增加运行时测试：
  - 启动 `SettingsActivity`；
  - 解析 AppCompat `colorPrimaryDark`；
  - 断言它等于 `tp_surface`；
  - 断言它不等于上游 `red_400/red_800`；
  - 断言实际 toolbar 背景是 `tp_surface`。

## 验收命令

```bash
source ./scripts/resolve-jdk17.sh
TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 \
  :app:testDebugUnitTest \
  --tests com.termux.app.ManifestProductIdentityTest \
  --tests com.termux.app.WorkspaceActivitySmokeTest.productPagesUseOpaqueSystemBarsAndLightDefaultText
```

结果：通过。

## 非目标

- 不改 Termux 原始终端主题。
- 不改共享模块默认红色主题，避免影响上游兼容页面。
- 不发布新版本；该改动并未达到单独候选/正式发布标准，随下一组可感知 UX 切片累计。
