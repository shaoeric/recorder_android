# 录音机 (Recorder)

一款 Android 录音应用，支持录音管理、时间标记、拍照关联与播放回放。

## 功能

- **录音列表** — 主界面展示所有录音，包含文件名、时长、占用空间、创建日期，按时间倒序排列
- **录音控制** — 底部悬浮菜单栏：开始录音 / 暂停 / 终止（暂停状态下长按 3 秒）
- **时间标记** — 录制中可随时添加时间标记，播放时进度条上以橙色圆点显示，点击跳转
- **拍照关联** — 录制中可拍摄照片，照片与当前时间点关联，播放时进度条上以蓝色圆点显示
- **播放回放** — 点击录音进入播放页，自动播放，支持播放/暂停、快进 3 秒、后退 3 秒、SeekBar 拖拽跳转
- **全屏照片** — 点击照片缩略图或进度条上的照片标记，全屏查看照片并自动跳转到对应时间点播放

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| 最低 SDK | API 26 (Android 8.0) |
| 架构 | MVVM + Repository |
| 数据库 | Room |
| 异步 | Kotlin Coroutines + Flow |
| DI | 手动依赖注入 |
| UI | ViewBinding + Material Components |
| 测试 | JUnit4 + Mockito + Robolectric + Room Testing + Espresso |

## 项目结构

```
app/src/main/java/com/recorder/app/
├── RecorderApplication.kt          # Application
├── data/
│   ├── entity/                     # Recording, TimeMarker, Photo
│   ├── dao/                        # Room DAO
│   ├── database/                   # AppDatabase
│   └── repository/                 # RecordingRepository
├── service/
│   ├── RecordingManager.kt         # 录音核心引擎
│   └── RecordingService.kt         # 前台 Service
├── ui/
│   ├── main/                       # 主界面 (列表 + 录音控制)
│   │   ├── MainActivity.kt
│   │   ├── MainViewModel.kt
│   │   └── RecordingAdapter.kt
│   └── playback/                   # 播放界面
│       ├── PlaybackActivity.kt
│       ├── PlaybackViewModel.kt
│       ├── PhotoAdapter.kt
│       └── PhotoFullscreenActivity.kt
└── util/
    ├── TimeUtils.kt                # 时间 / 文件大小格式化
    └── FileUtils.kt                # 文件目录 / 命名
```

## 构建

### 前置条件

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 步骤

```bash
# 克隆仓库
git clone https://github.com/shaoeric/recorder_android.git
cd recorder_android

# 用 Android Studio 打开项目，Sync Gradle 后即可运行
```

### 命令行构建

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (需配置签名)
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 测试

```bash
# 运行单元测试
./gradlew test

# 运行仪器化测试（需要模拟器或真机）
./gradlew connectedAndroidTest

# 生成覆盖率报告
./gradlew testDebugUnitTestCoverage
```

## 权限

| 权限 | 用途 |
|------|------|
| `RECORD_AUDIO` | 录音 |
| `CAMERA` | 拍照 |
| `FOREGROUND_SERVICE` | 录音前台服务 |
| `POST_NOTIFICATIONS` | 录音通知 (Android 13+) |

## CI/CD

本仓库包含 GitHub Actions 工作流，支持手动触发构建 APK 并自动发布到 Release。

触发方式：GitHub 仓库页面 → **Actions** → **Build APK** → **Run workflow**

## 截图

> 即将上线
