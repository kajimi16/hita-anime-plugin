# HITA Anime Plugin

OH-MY-HITA 的外部番剧插件（独立仓库，不随宿主发版）。

- 插件 id：`com.stupidtree.hita.ui.anime`
- 形态：运行时界面插件（`ComposeUiPlugin`）
- 契约：`apiVersion 5` / Compose `1.6.8`
- 版本：`0.4.0`

## 功能

- **首页**：今日课表（宿主 `TimetableService`）、今日放送、本周放送、继续追番
- **放送表**：Bangumi `calendar` 当前在播番组，按星期分组（含长篇连载）
- **搜索**：关键词 + 类型 + 排序 + 年份/月份 + 标签，分页
- **追番**：五种收藏状态、进度、评分
- **条目详情**：评分直方图、标签/作品信息、章节格子、章节进度、收藏/评分/短评、
  角色（CV）/制作/关联、弹幕、校内评论、外部资源索引
- **弹幕**：hit-ani 本地 + Animeko/dandanplay 外部源；只看本校；发送
- **缓存回退**：优先 hit-ani `/api/bgm` 与 `/_next/image`，失败直连 BGM；
  站点地址支持逗号分隔多个缓存服务器
- **清晰封面**：按显示尺寸 320/640/1080 分档，`large` 原图，`ARGB_8888`

## 构建

```bash
./gradlew :anime-plugin:assembleRelease
```

本地调试（宿主 debug 包）：

- 用 OH-MY-HITA 的 `scripts/dev-install-ui-plugin.sh` 广播安装
- 或把 APK 放到本地 HTTP 索引，宿主以 `-PpluginIndexUrl=` 指向它

单元测试：

```bash
./gradlew :anime-plugin:testReleaseUnitTest
```

## 依赖

`libs/` 下的 `api-release.aar` / `model-release.aar` / `component-release.aar`
来自 OH-MY-HITA 发布构建；插件对它们以及 Compose、协程、`kotlin-stdlib`、
`material-icons-core` 使用 `compileOnly`，运行时全部由宿主提供。

## 发布请求（外部作者流程）

本仓库不直接改市场索引，也不自行发布。维护者发布流程：

1. 由维护者在 `kajimi16/hita-community-plugins` 的 Actions 手动运行
   **Publish Plugin**，填以下元数据
2. CI 只读构建本仓库，产物写入社区仓库 `dist/<id>-<version>.apk`
3. CI 校验 `assets/plugin.json`、计算 sha256、更新 `registry/index.json`

请求参数：

| 字段 | 值 |
|---|---|
| repo | `kajimi16/hita-anime-plugin` |
| ref | `master` |
| module | `:anime-plugin` |
| kind | `ui` |
| plugin_id | `com.stupidtree.hita.ui.anime` |
| name | `番剧` |
| version | `0.4.0` |
| entry_class | `com.stupidtree.hita.ui.anime.AnimeUiPlugin` |
| min_host_api | `5` |
| tags | `bangumi,anime,danmaku` |

## 0.4.0 变更（UI 对齐 Animeko）

- 设计 token：Animeko 默认种子色 `#4F378B`、M3 紫色明暗配色、9:16 海报、
  大圆角、标题层级与间距；移除 Material 2 默认观感
- 首页：紧凑课表卡、继续观看横向大卡、放送表日分段、今日更新海报行、
  我的收藏海报墙
- 条目详情：封面 + 渐变头部、分段 tab、章节格子、评分直方图、角色横滑
- 搜索：年份/季度/标签 chip，双列 9:16 海报网格与封面角标
- 追番：状态 tab + 双列海报网格，替换列表行
- 顶部导航改为胶囊分段式（宿主底部导航固定，插件不能占用）

## 已知限制

- 插件不能声明 Activity，播放交给外站/外部播放器；不包含 BT/离线缓存
- 宿主底部导航会盖住运行时 tab 的底部区域，因此插件内部导航放在顶部
- 收藏/进度写入仍直连 Bangumi，需要用户自己的访问令牌

## License

MIT
