#!/bin/bash
# customize-bv-frost819.sh
# 简化版本：直接修改 AppConfiguration.kt 文件中的配置

# 修改 AppConfiguration.kt 中的两个配置项
sed -i \
  -e 's/const val minSdk = 23/const val minSdk = 21/' \
  -e 's/"git rev-list --count HEAD".exec().toInt() - 5/"git rev-list --count HEAD".exec().toInt() + 1/' \
  buildSrc/src/main/kotlin/AppConfiguration.kt