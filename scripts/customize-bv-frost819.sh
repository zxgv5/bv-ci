#!/bin/bash
# customize-bv-frost819.sh
# 用于对源代码进行构建前的自定义修改

echo "开始执行自定义构建前修改脚本..."

# 1. 修改minSdk从23到21
sed -i 's/const val minSdk = 23/const val minSdk = 21/g' buildSrc/src/main/kotlin/AppConfiguration.kt
echo "✓ 已将minSdk从23修改为21"

# 2. 这里可以添加更多修改，例如：
# sed -i 's/其他需要替换的内容/新内容/g' 目标文件路径
# 或者执行其他构建前准备任务

echo "自定义修改完成"