# 终端远程配置入口语义验收

## 背景

0.10.0 真实体验反馈指出：终端页进入工作台的入口不够直观，用户第一次容易把顶部入口理解成无关按钮，
从而找不到返回远程配置、工作区和连接策略的位置。

## 调整

- 将终端顶部 `工作台` 文案改为 `远程配置`。
- 将无障碍描述从抽象的“返回服务器、项目目录和远程入口”改为“返回远程工作区配置，管理服务器、项目目录和连接策略”。
- 保留 Manifest 产品名回归，桌面启动名仍必须是 `TermuxPro`，不能被首页标题或入口文案污染。

## 验收

- `CustomLayoutsSmokeTest.terminalNavigationUsesPersistentLabelsAndAccessibleTargets` 校验终端顶部入口包含“远程”和“配置”语义。
- `ManifestProductIdentityTest` 校验桌面和设置页仍使用 TermuxPro 产品身份与非危险主题。
- `UiRenderingInstrumentedTest.captureCriticalDarkPages` 同步校验真实 Android 渲染截图里的终端顶栏入口文案，避免 JVM 测试通过但模拟器截图门禁仍按旧文案失败。

## 本地验证

```text
source ./scripts/resolve-jdk17.sh && TERMUXPRO_USE_CHINA_MIRROR=1 ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest --tests com.termux.app.CustomLayoutsSmokeTest --tests com.termux.app.ManifestProductIdentityTest

BUILD SUCCESSFUL in 24s
```
