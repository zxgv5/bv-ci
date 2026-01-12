#!/bin/bash
# customize-bv-fantasy.sh

# ========== 核心调试配置（替换原set -e） ==========
# set -e: 错误立即退出 | set -x: 输出执行的每条命令 | set -E: 错误陷阱继承到函数
set -eEx
# 错误捕获陷阱：出错时输出关键调试信息
trap '
  echo -e "\n====================================="
  echo -e "           ERROR DEBUG INFO           "
  echo -e "====================================="
  echo "  执行失败的命令: $BASH_COMMAND"
  echo "  命令退出码: $?"
  echo "  出错行号: $LINENO"
  echo "  脚本文件: $BASH_SOURCE"
  echo "  当前工作目录: $(pwd)"
  echo -e "=====================================\n"
  exit 1
' ERR

# ========== 原有变量定义 ==========
FANTASY_BV_SOURCE_ROOT="$GITHUB_WORKSPACE/fantasy-bv-source"

# 修改 AppConfiguration.kt 中的配置项
# 版本号规则调整，避免负数
# 修改包名
FANTASY_BV_SOURCE_BSMK_APPCONFIGURATION="$FANTASY_BV_SOURCE_ROOT/buildSrc/src/main/kotlin/AppConfiguration.kt"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_BSMK_APPCONFIGURATION"
if [ ! -f "$FANTASY_BV_SOURCE_BSMK_APPCONFIGURATION" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
sed -i \
  -e 's/"git rev-list --count HEAD".exec().toInt() - 5/"git rev-list --count HEAD".exec().toInt() + 1/' \
  -e 's/const val applicationId = "dev.aaa1115910.bv2"/const val applicationId = "dev.fantasy.bv"/' \
  "$FANTASY_BV_SOURCE_BSMK_APPCONFIGURATION"
echo "[DEBUG] 处理完成: AppConfiguration.kt"

# 修改应用名
FANTASY_BV_SOURCE_ASSDRV_STRINGS="$FANTASY_BV_SOURCE_ROOT/app/shared/src/debug/res/values/strings.xml"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_ASSDRV_STRINGS"
if [ ! -f "$FANTASY_BV_SOURCE_ASSDRV_STRINGS" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
sed -i 's/<string[[:space:]]*name="app_name"[[:space:]]*>.*BV Debug.*<\/string>/<string name="app_name">fantasy Debug<\/string>/' "$FANTASY_BV_SOURCE_ASSDRV_STRINGS"
echo "[DEBUG] 处理完成: debug strings.xml"

FANTASY_BV_SOURCE_ASSMRV_STRINGS="$FANTASY_BV_SOURCE_ROOT/app/shared/src/main/res/values/strings.xml"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_ASSMRV_STRINGS"
if [ ! -f "$FANTASY_BV_SOURCE_ASSMRV_STRINGS" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
sed -i 's/<string[[:space:]]*name="app_name"[[:space:]]*>.*BV.*<\/string>/<string name="app_name">fantasy<\/string>/' "$FANTASY_BV_SOURCE_ASSMRV_STRINGS"
echo "[DEBUG] 处理完成: main strings.xml"

FANTASY_BV_SOURCE_ASSRRV_STRINGS="$FANTASY_BV_SOURCE_ROOT/app/shared/src/r8Test/res/values/strings.xml"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_ASSRRV_STRINGS"
if [ ! -f "$FANTASY_BV_SOURCE_ASSRRV_STRINGS" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
sed -i 's/<string[[:space:]]*name="app_name"[[:space:]]*>.*BV R8 Test.*<\/string>/<string name="app_name">fantasy R8 Test<\/string>/' "$FANTASY_BV_SOURCE_ASSRRV_STRINGS"
echo "[DEBUG] 处理完成: r8Test strings.xml"

# 尝试修复“动态”页面长按下方向键焦点左移出视频选择区的问题
# 先修改DynamicsScreen.kt源代码
FANTASY_BV_SOURCE_PTSMKDABPTCP_ATSMKDABTSMH_DYNAMICSSCREEN="$FANTASY_BV_SOURCE_ROOT/app/tv/src/main/kotlin/dev/aaa1115910/bv/tv/screens/main/home/DynamicsScreen.kt"
CI_CUSTOMIZE_SCRIPTS_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
FANTASY_BV_SOURCE_PTSMKDABPTCP_ATSMKDABTSMH_DYNAMICSSCREEN_PYTHON_SCRIPT="$CI_CUSTOMIZE_SCRIPTS_DIR/modify_bv_fantasy_dynamics_screen.py"
echo -e "\n[DEBUG] 脚本所在目录: $CI_CUSTOMIZE_SCRIPTS_DIR"

# 1. 检查python环境（确保ci已安装python3）
echo -e "\n===== 检查python环境 ====="
if ! command -v python3 &> /dev/null; then
  echo "错误：未找到python3，请检查ci流程中的python安装步骤！"
  exit 1
fi
python3 --version  # 输出版本，便于调试

# 2. 检查python脚本是否存在
echo -e "\n===== 准备执行python脚本：$FANTASY_BV_SOURCE_PTSMKDABPTCP_ATSMKDABTSMH_DYNAMICSSCREEN_PYTHON_SCRIPT ====="
if [ ! -f "$FANTASY_BV_SOURCE_PTSMKDABPTCP_ATSMKDABTSMH_DYNAMICSSCREEN_PYTHON_SCRIPT" ]; then
  echo "错误：python脚本 $FANTASY_BV_SOURCE_PTSMKDABPTCP_ATSMKDABTSMH_DYNAMICSSCREEN_PYTHON_SCRIPT 不存在！"
  exit 1
fi

# 3. 检查目标kotlin文件是否存在
if [ ! -f "$FANTASY_BV_SOURCE_PTSMKDABPTCP_ATSMKDABTSMH_DYNAMICSSCREEN" ]; then
  echo "错误：目标文件 $FANTASY_BV_SOURCE_PTSMKDABPTCP_ATSMKDABTSMH_DYNAMICSSCREEN 不存在！"
  exit 1
fi

# 4. 执行python脚本（传递目标文件路径作为【位置参数】，去掉--target-file）
echo -e "\n===== 执行python脚本处理DynamicsScreen.kt ====="
python3 "$FANTASY_BV_SOURCE_PTSMKDABPTCP_ATSMKDABTSMH_DYNAMICSSCREEN_PYTHON_SCRIPT" "$FANTASY_BV_SOURCE_PTSMKDABPTCP_ATSMKDABTSMH_DYNAMICSSCREEN"

# 5. 校验python脚本执行结果
if [ $? -eq 0 ]; then
  echo -e "\n===== python脚本执行成功 ====="
else
  echo "错误：python脚本执行失败！"
  exit 1
fi

# ========== 处理 libs.versions.toml（重点排查对象） ==========
FANTASY_BV_SOURCE_GRADLE_LVT="$FANTASY_BV_SOURCE_ROOT/gradle/libs.versions.toml"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_GRADLE_LVT"
if [ ! -f "$FANTASY_BV_SOURCE_GRADLE_LVT" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
# 移除行内注释，避免参数解析干扰
sed -i \
  -e '/zxing = "3.5.3"/a\androidxComposeBom = "2025.12.00"\nandroidxTvFoundation = "1.1.0"' \
  -e '/zxing = { module = "com.google.zxing:core", version.ref = "zxing" }/a\\n# Compose BOM\androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "androidxComposeBom" }\n# AndroidX TV Foundation\androidx-tv-foundation = { module = "androidx.tv:tv-foundation", version.ref = "androidxTvFoundation" }' \
  "$FANTASY_BV_SOURCE_GRADLE_LVT"
echo "[DEBUG] 处理完成: libs.versions.toml"

# ========== 处理 player/tv/build.gradle.kts ==========
FANTASY_BV_SOURCE_PT_BGK="$FANTASY_BV_SOURCE_ROOT/player/tv/build.gradle.kts"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_PT_BGK"
if [ ! -f "$FANTASY_BV_SOURCE_PT_BGK" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
sed -i \
  -e '/dependencies {/a\    val composeBom = platform(libs.androidx.compose.bom)\n    implementation(composeBom)\n    androidTestImplementation(composeBom)' \
  -e 's/implementation(androidx.compose.tv.foundation)/implementation(libs.androidx.tv.foundation)/' \
  "$FANTASY_BV_SOURCE_PT_BGK"
echo "[DEBUG] 处理完成: player/tv/build.gradle.kts"

# ========== TV端倍速范围调整 ==========
FANTASY_BV_SOURCE_PTSMKDABPTCP_PICTUREMENU="$FANTASY_BV_SOURCE_ROOT/player/tv/src/main/kotlin/dev/aaa1115910/bv/player/tv/controller/playermenu/PictureMenu.kt"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_PTSMKDABPTCP_PICTUREMENU"
if [ ! -f "$FANTASY_BV_SOURCE_PTSMKDABPTCP_PICTUREMENU" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
sed -i '/VideoPlayerPictureMenuItem\.PlaySpeed ->/,/^[[:space:]]*)/s/range = 0\.25f\.\.3f/range = 0.25f..5f/' "$FANTASY_BV_SOURCE_PTSMKDABPTCP_PICTUREMENU"
echo "[DEBUG] 处理完成: PictureMenu.kt"

# ========== 焦点逻辑更改 ==========
FANTASY_BV_SOURCE_PTSMKDABPTC_CONTROLLERVIDEOINFO="$FANTASY_BV_SOURCE_ROOT/player/tv/src/main/kotlin/dev/aaa1115910/bv/player/tv/controller/ControllerVideoInfo.kt"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_PTSMKDABPTC_CONTROLLERVIDEOINFO"
if [ ! -f "$FANTASY_BV_SOURCE_PTSMKDABPTC_CONTROLLERVIDEOINFO" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
sed -i 's/^\([[:space:]]*\)down = focusRequesters\[if (showNextVideoBtn) "nextVideo" else "speed"\] ?: FocusRequester()/\1down = focusRequesters["danmaku"] ?: FocusRequester()/' "$FANTASY_BV_SOURCE_PTSMKDABPTC_CONTROLLERVIDEOINFO"
echo "[DEBUG] 处理完成: ControllerVideoInfo.kt"

# ========== 隐藏左边侧边栏页面 ==========
FANTASY_BV_SOURCE_ATSMKDABTSM_DRAWERCONTENT="$FANTASY_BV_SOURCE_ROOT/app/tv/src/main/kotlin/dev/aaa1115910/bv/tv/screens/main/DrawerContent.kt"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_ATSMKDABTSM_DRAWERCONTENT"
if [ ! -f "$FANTASY_BV_SOURCE_ATSMKDABTSM_DRAWERCONTENT" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
sed -i \
  -e 's/^\([[:space:]]*\)DrawerItem\.Search,/\1\/\/DrawerItem.Search,/' \
  -e 's/^\([[:space:]]*\)DrawerItem\.UGC,/\1\/\/DrawerItem.UGC,/' \
  -e 's/^\([[:space:]]*\)DrawerItem\.PGC,/\1\/\/DrawerItem.PGC,/' \
  "$FANTASY_BV_SOURCE_ATSMKDABTSM_DRAWERCONTENT"
echo "[DEBUG] 处理完成: DrawerContent.kt"

# ========== 隐藏顶上导航标签 ==========
FANTASY_BV_SOURCE_ATSMKDABTC_TOPNAV="$FANTASY_BV_SOURCE_ROOT/app/tv/src/main/kotlin/dev/aaa1115910/bv/tv/component/TopNav.kt"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_ATSMKDABTC_TOPNAV"
if [ ! -f "$FANTASY_BV_SOURCE_ATSMKDABTC_TOPNAV" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
sed -i \
  -e 's/^\([[:space:]]*\)Favorite("收藏"),[[:space:]]*$/\1Favorite("收藏");/' \
  -e 's/^\([[:space:]]*\)FollowingSeason("追番"),[[:space:]]*$/\/\/\1FollowingSeason("追番"),/' \
  -e 's/^\([[:space:]]*\)ToView("稍后看");[[:space:]]*$/\/\/\1ToView("稍后看");/' \
  "$FANTASY_BV_SOURCE_ATSMKDABTC_TOPNAV"
echo "[DEBUG] 处理完成: TopNav.kt"

# ========== 配合隐藏导航标签的修改 ==========
FANTASY_BV_SOURCE_ATSMKDABTSM_HOMECONTENT="$FANTASY_BV_SOURCE_ROOT/app/tv/src/main/kotlin/dev/aaa1115910/bv/tv/screens/main/HomeContent.kt"
echo -e "\n[DEBUG] 处理文件: $FANTASY_BV_SOURCE_ATSMKDABTSM_HOMECONTENT"
if [ ! -f "$FANTASY_BV_SOURCE_ATSMKDABTSM_HOMECONTENT" ]; then
  echo "ERROR: 文件不存在!"
  exit 1
fi
# 使用perl一次性处理6个多行替换
perl -i -0777 -pe '
  s{                HomeTopNavItem\.FollowingSeason -> \{
//                    if \(followingSeasonViewModel\.followingSeasons\.isEmpty\(\) && userViewModel\.isLogin\) \{
//                        followingSeasonViewModel\.loadMore\(\)
//                    \}
                \}}{//                HomeTopNavItem.FollowingSeason -> {
//                    if (followingSeasonViewModel.followingSeasons.isEmpty() && userViewModel.isLogin) {
//                        followingSeasonViewModel.loadMore()
//                    }
//                }}sg;
  s{                HomeTopNavItem\.ToView -> \{
//                    if \(toViewViewModel\.histories\.isEmpty\(\) && userViewModel\.isLogin\) \{
//                        toViewViewModel\.update\(\)
//                    \}
                \}}{//                HomeTopNavItem.ToView -> {
//                    if (toViewViewModel.histories.isEmpty() && userViewModel.isLogin) {
//                        toViewViewModel.update()
//                    }
//                }}sg;
  s{                        HomeTopNavItem\.FollowingSeason -> \{
                            if \(userViewModel\.isLogin\) \{
                                followingSeasonViewModel\.clearData\(\)
                                followingSeasonViewModel\.loadMore\(\)
                            \}
                        \}}{//                        HomeTopNavItem.FollowingSeason -> {
//                            if (userViewModel.isLogin) {
//                                followingSeasonViewModel.clearData()
//                                followingSeasonViewModel.loadMore()
//                            }
//                        }}sg;
  s{                        HomeTopNavItem\.ToView -> \{
                            if \(userViewModel\.isLogin\) \{
                                toViewViewModel\.clearData\(\)
                                toViewViewModel\.update\(\)
                            \}
                        \}}{//                        HomeTopNavItem.ToView -> {
//                            if (userViewModel.isLogin) {
//                                toViewViewModel.clearData()
//                                toViewViewModel.update()
//                            }
//                        }}sg;
  s{                    HomeTopNavItem\.FollowingSeason -> \{
                        if \(userViewModel\.isLogin\) \{
                            FollowingSeasonScreen\(showPageTitle = false\)
                        \} else \{
                            LoginRequiredScreen\(\)
                        \}
                    \}}{//                    HomeTopNavItem.FollowingSeason -> {
//                        if (userViewModel.isLogin) {
//                            FollowingSeasonScreen(showPageTitle = false)
//                        } else {
//                            LoginRequiredScreen()
//                        }
//                    }}sg;
  s{                    HomeTopNavItem\.ToView -> \{
                        if \(userViewModel\.isLogin\) \{
                            ToViewScreen\(showPageTitle = false\)
                        \} else \{
                            LoginRequiredScreen\(\)
                        \}
                    \}}{//                    HomeTopNavItem.ToView -> {
//                        if (userViewModel.isLogin) {
//                            ToViewScreen(showPageTitle = false)
//                        } else {
//                            LoginRequiredScreen()
//                        }
//                    }}sg;
' "$FANTASY_BV_SOURCE_ATSMKDABTSM_HOMECONTENT"
echo "[DEBUG] 处理完成: HomeContent.kt (perl替换)"

# 还有两行没有注释掉，补上
echo -e "\n[DEBUG] 补充处理 HomeContent.kt 剩余行"
sed -i \
  -e 's/^\([[:space:]]*\)HomeTopNavItem\.FollowingSeason -> followingSeasonState$/\1\/\/HomeTopNavItem.FollowingSeason -> followingSeasonState/' \
  -e 's/^\([[:space:]]*\)HomeTopNavItem\.ToView -> toViewState$/\1\/\/HomeTopNavItem.ToView -> toViewState/' \
  "$FANTASY_BV_SOURCE_ATSMKDABTSM_HOMECONTENT"
echo "[DEBUG] 处理完成: HomeContent.kt (剩余行)"

echo -e "\n====================================="
echo "        所有脚本处理完成！"
echo -e "====================================="
