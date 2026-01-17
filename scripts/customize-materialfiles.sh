#!/bin/bash
# customize-materialfiles.sh

# materialfile.patch.md
# # 问题
# 添加SMB服务器时通过自动搜索选择Windows服务器后验证方式选择访客或匿名.
# # 报错
# java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.Integer
# ---
# # 建议
# 升级库到最新版:0.14.0
# 由于我不懂安卓开发,更加不知道升级库是否会带来其他问题,不敢提交PR.
# 目前测试下来我需要的功能工作正常,这个文件管理器对我来说很完美了.
# ---
# 1、app/build.gradle
# // 引入smbj依赖，排除冲突的bouncycastle模块
#     implementation ('com.hierynomus:smbj:0.14.0') {
#         // 原因：org.bouncycastle:bcprov-jdk15on 的字节码版本不被Jetifier支持，建议使用 bcprov-jdk15to18 替代
#         exclude group: 'org.bouncycastle', module: 'bcprov-jdk15on'
#         // 增加此行，避免编译失败
#         exclude group: 'org.bouncycastle', module: 'bcprov-jdk18on'
#     }
#     // 引入dcerpc依赖，排除冲突的guava和bouncycastle模块
#     implementation ('com.rapid7.client:dcerpc:0.12.1') {
#         // 原因：SMBJ-RPC依赖的Guava为JRE版本，目标Java 8
#         exclude group: 'com.google.guava', module: 'guava'
#         exclude group: 'org.bouncycastle', module: 'bcprov-jdk15on'
#         // 增加此行，避免编译失败
#         exclude group: 'org.bouncycastle', module: 'bcprov-jdk18on'
#     }
# 2、app/src/main/java/me/zhanghai/android/files/provider/smb/client/FileByteChannel.kt
# source.position(sourcePosition + bytesWritten)
# 改为
# source.position((sourcePosition + bytesWritten).toInt())
# ---
# # 作者回复
# 据说 SMBJ 1.14.0 还是有问题，见 hierynomus/smbj#792 (comment) ，所以我没有升级。

set -e  # 遇到错误立即退出，避免ci静默失败
 
MATERIALFILES_SOURCE_ROOT="$GITHUB_WORKSPACE/materialfiles_source"
 
MATERIALFILES_APP_BUILDGRADLE="$MATERIALFILES_SOURCE_ROOT/app/build.gradle"
CI_MATERIALFILES_APP_BUILDGRADLE="$GITHUB_WORKSPACE/ci_source/patches/materialfiles/ci_app_build.gradle"
if [ ! -f "$CI_MATERIALFILES_APP_BUILDGRADLE" ]; then
    echo "❌ 错误：源文件 $CI_MATERIALFILES_APP_BUILDGRADLE 不存在"
    exit 1
fi
cp -f "$CI_MATERIALFILES_APP_BUILDGRADLE" "$MATERIALFILES_APP_BUILDGRADLE"

MATERIALFILES_FILEBYTECHANNEL_KT="$MATERIALFILES_SOURCE_ROOT/app/src/main/java/me/zhanghai/android/files/provider/smb/client/FileByteChannel.kt"
CI_MATERIALFILES_FILEBYTECHANNEL_KT="$GITHUB_WORKSPACE/ci_source/patches/materialfiles/ci_FileByteChannel.kt"
if [ ! -f "$CI_MATERIALFILES_FILEBYTECHANNEL_KT" ]; then
    echo "❌ 错误：源文件 $CI_MATERIALFILES_FILEBYTECHANNEL_KT 不存在"
    exit 1
fi
cp -f "$CI_MATERIALFILES_FILEBYTECHANNEL_KT" "$MATERIALFILES_FILEBYTECHANNEL_KT"
