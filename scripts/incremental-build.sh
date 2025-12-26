#!/bin/bash

# 增量构建脚本
set -e

BUILD_TYPE=${1:-incremental}
PROJECT_DIR=${2:-.}

cd "$PROJECT_DIR"

echo "构建类型: $BUILD_TYPE"
echo "项目目录: $PROJECT_DIR"

# 检查是否需要构建
if [ "$BUILD_TYPE" = "incremental" ]; then
    # 获取最近修改的文件
    CHANGED_FILES=$(git diff --name-only HEAD~1 HEAD 2>/dev/null || echo "")
    
    if [ -z "$CHANGED_FILES" ]; then
        echo "没有检测到代码变更，跳过构建"
        exit 0
    fi
    
    echo "检测到变更的文件:"
    echo "$CHANGED_FILES"
    
    # 智能判断需要构建的模块
    if echo "$CHANGED_FILES" | grep -q "akdanmaku/"; then
        echo "检测到akdanmaku模块变更"
        ./gradlew :akdanmaku:assembleRelease \
            --build-cache \
            --configuration-cache \
            --parallel
    fi
    
    if echo "$CHANGED_FILES" | grep -q "app/"; then
        echo "检测到app模块变更"
        ./gradlew :app:assembleRelease \
            --build-cache \
            --configuration-cache \
            --parallel
    fi
else
    # 全量构建
    ./gradlew assembleRelease --parallel
fi