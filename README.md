# DeepSeek Balance

DeepSeek API 余额查询工具 - Android 桌面应用

## 功能特性

✨ **余额查询** - 实时查询 DeepSeek 账户余额  
📱 **桌面小组件** - 在桌面直接显示余额，支持自定义更新间隔  
🔄 **自动刷新** - 支持下拉刷新和定时自动更新  
🔒 **安全可靠** - API Key 仅存储在本地，数据加密传输

## 开发环境

- **Android SDK**: API 36 (Android 16)
- **Kotlin**: 2.0.21
- **构建工具**: Gradle 8.9
- **最低兼容**: Android 16 (API 36) - 64位

## 构建方法

### 方法一：Android Studio（推荐）

1. **克隆/下载** 本项目到本地
2. 使用 **Android Studio** 打开 `DeepSeekBalance` 目录
3. 等待 Gradle 同步完成
4. 点击菜单 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
5. 生成的 APK 在 `app/build/outputs/apk/debug/` 目录下

### 方法二：命令行构建

```bash
# 设置 ANDROID_HOME 环境变量指向你的 SDK 目录
export ANDROID_HOME=/path/to/android/sdk

# 构建 Debug APK
./gradlew assembleDebug

# 或构建 Release APK（需要签名配置）
./gradlew assembleRelease
```

> ⚠️ 需要提前安装 Android SDK 36 及 Build Tools

## 使用说明

1. **首次打开 App** → 输入你的 DeepSeek API Key
2. **查看余额** → 自动查询并显示总余额、充值余额、赠送余额
3. **添加小组件** → 
   - 长按桌面空白处 → 选择"小组件" → 找到"DeepSeek 余额"
   - 拖拽到桌面即可
4. **设置更新间隔** → 在 App 内点击"桌面小组件"设置

## 技术栈

| 组件 | 选型 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Material 3 + ConstraintLayout |
| 网络请求 | Retrofit 2 + OkHttp 4 |
| 数据解析 | Gson |
| 后台任务 | WorkManager |
| 数据存储 | SharedPreferences |

## 隐私说明

- 所有数据仅存储在你的设备上
- API Key 不会上传至任何第三方服务器
- 网络请求仅发送到 `api.deepseek.com`
