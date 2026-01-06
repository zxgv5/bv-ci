#!/bin/bash
# customize-bv-frost819.sh

FROST819_BV_SOURCE_ROOT="$GITHUB_WORKSPACE/frost819-bv-source"

FROST819_BV_SOURCE_APPCONFIGURATION="$FROST819_BV_SOURCE_ROOT/buildSrc/src/main/kotlin/AppConfiguration.kt"
# 修改 AppConfiguration.kt 中的配置项
sed -i \
  -e 's/const val minSdk = 21/const val minSdk = 23/' \
  -e 's/"git rev-list --count HEAD".exec().toInt() - 5/"git rev-list --count HEAD".exec().toInt() + 1/' \
  -e 's/const val applicationId = "dev.frost819.bv"/const val applicationId = "dev.f819.bv"/' \
  "$FROST819_BV_SOURCE_APPCONFIGURATION"

FROST819_BV_SOURCE_ASDRV_STRINGS="$FROST819_BV_SOURCE_ROOT/app/src/debug/res/values/strings.xml"
sed -i 's/<string[[:space:]]*name="app_name"[[:space:]]*>.*BV Debug.*<\/string>/<string name="app_name">f819 Debug<\/string>/' "$FROST819_BV_SOURCE_ASDRV_STRINGS"

FROST819_BV_SOURCE_ASMRV_STRINGS="$FROST819_BV_SOURCE_ROOT/app/src/main/res/values/strings.xml"
sed -i 's/<string[[:space:]]*name="app_name"[[:space:]]*>.*BV.*<\/string>/<string name="app_name">f819<\/string>/' "$FROST819_BV_SOURCE_ASMRV_STRINGS"

FROST819_BV_SOURCE_ASRRV_STRINGS="$FROST819_BV_SOURCE_ROOT/app/src/r8Test/res/values/strings.xml"
sed -i 's/<string[[:space:]]*name="app_name"[[:space:]]*>.*BV R8 Test.*<\/string>/<string name="app_name">f819 R8 Test<\/string>/' "$FROST819_BV_SOURCE_ASRRV_STRINGS"