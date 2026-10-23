# TaskFlow · Android 企业任务管理

Java 原生 Android 应用，采用 MVVM + Repository + ViewModel + LiveData + Room。按需求实现本地任务管理，不包含账号、服务器或多人实时同步。

## 运行

1. 使用 Android Studio 打开本目录，安装 Android SDK Platform 34，并使用 JDK 17 或 Android Studio 内置 JDK。
2. 等待 Gradle Sync，选择 Android 8.0（API 26）或更高版本设备，运行 app。
3. 命令行：`./gradlew assembleDebug testDebugUnitTest`。APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

`local.properties` 为当前机器的 SDK 路径；迁移到其他机器时删除它，由 Android Studio 自动生成。首次构建需联网下载依赖。压缩包附带仅用于调试的 `.debug/debug.keystore`，密码为 Android 默认调试密码；不适用于正式发布。通过 Git 克隆时不会包含此密钥，项目会自动采用 Android 标准调试签名。构建采用 Gradle 8.9、AGP 8.7.2、Room 2.6.1。

## 功能和操作

- 创建/编辑任务：标题必填（最多100字），描述最多2000字，支持低/中/高优先级和可选截止日期、时分。
- 点击开始任务：TODO → IN_PROGRESS；点击标记完成：IN_PROGRESS → DONE；已完成任务可重新打开为 TODO。
- 搜索标题及描述（忽略大小写、首尾空格），与状态筛选同时生效。关键词采用字面匹配。
- 删除前二次确认；删除后无法恢复。
- Room 持久化，重启后保留；后台串行执行写入，写入错误显示提示。
- 空列表、搜索无结果时展示提示。旋转屏幕后 ViewModel 保留搜索和筛选；未保存的编辑弹窗内容不作持久化。

## 动态排序

未完成任务评分：`优先级 × 100 + 状态分 + 截止时间分`。

| 因素 | 得分 |
|---|---:|
| 低 / 中 / 高优先级 | 100 / 200 / 300 |
| 进行中 | +30 |
| 已逾期 | +400 |
| 距截止 ≤24小时 | +300 |
| 距截止 ≤3天 | +200 |
| 距截止 ≤7天 | +100 |
| 更远或无截止日期 | +0 |
| 已完成 | 总分固定为 -1 |

截止时间区间互斥。按评分降序，同分按截止时间升序（无截止时间排后），再按创建时间降序及 ID 升序。每分钟以及回到前台时重新计算。例如低优先级逾期任务得500分，高优先级无截止日期任务得300分。已完成任务始终排在未完成任务之后。截止时间按设备本地时区输入，以时间戳保存；应用未运行时不执行定时提醒。

## 架构

`ui/MainActivity`：界面和输入，生命周期感知观察 LiveData。

`ui/TaskViewModel`：搜索/筛选状态、自动重排和用户操作入口。

`domain/TaskRanker`：独立可测试的匹配及评分规则。

`data/TaskRepository`：统一数据库访问、后台写入和错误反馈。

`data/TaskDatabase + TaskDao + Task`：Room 实体、DAO、单例数据库。数据库版本1，导出 schema；后续升级须添加 Migration，不使用破坏性迁移。

数据流：用户操作 → ViewModel → Repository → DAO → Room → LiveData → ViewModel → 界面。

## 验收步骤

1. 新建低优先级逾期任务及高优先级无截止日期任务，检查逾期任务在前。
2. 修改标题/描述/优先级/截止时间，检查列表及评分刷新。
3. 输入关键词并切换状态，确认同时过滤；输入不存在关键词，检查空状态。
4. 依次点击开始、完成、重新打开，确认状态和排序更新。
5. 删除时分别取消和确认，检查结果；强制关闭并重启，检查数据保留。
6. 旋转屏幕，确认列表/搜索/筛选保持；后台放置跨越截止分界后返回，确认评分刷新。

自动测试覆盖逾期、完成、动态时间、24小时边界、进行中加分、组合搜索筛选与排序平局。设备验收需要连接真机或启动模拟器。

## 技术参考

[Android 官方 Room Java 依赖与注解处理说明](https://developer.android.com/jetpack/androidx/releases/room)。本项目固定使用支持 Java annotationProcessor 的 Room 2.x。
