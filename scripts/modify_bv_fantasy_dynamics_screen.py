import os
import sys

def modify_libs_versions_toml(file_path):
    """修改gradle/libs.versions.toml文件：使用稳定依赖版本 + BOM统一管理"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        # 步骤1：在[libraries]前添加4行版本定义（核心修改：降低tv版本为稳定版0.6.0）
        insert_lines_version = [
            'androidx-compose = "1.6.0"  # Compose 核心版本\n',
            'androidx-compose-bom = "2024.02.02"  # Compose BOM 版本\n',
            'androidx-tv = "0.6.0"  # TV Compose 稳定版本（1.0.0暂未发布）\n',
            'androidx-lifecycle = "2.7.0"  # Lifecycle 版本\n'
        ]
        libraries_index = None
        for idx, line in enumerate(lines):
            if line.strip() == '[libraries]':
                libraries_index = idx
                break
        if libraries_index is not None:
            for line in reversed(insert_lines_version):
                lines.insert(libraries_index, line)
        
        # 步骤2：在文件末尾追加依赖（核心修改：移除material3手动版本，由BOM管理）
        append_lines = [
            '# 添加的 Compose 相关依赖\n',
            '# Compose BOM（统一管理所有Compose版本）\n',
            'androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "androidx-compose-bom" }\n',
            '# Compose 基础依赖\n',
            'androidx-compose-ui = { module = "androidx.compose.ui:ui", version.ref = "androidx-compose" }\n',
            'androidx-compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics", version.ref = "androidx-compose" }\n',
            'androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview", version.ref = "androidx-compose" }\n',
            'androidx-compose-foundation = { module = "androidx.compose.foundation:foundation", version.ref = "androidx-compose" }\n',
            'androidx-compose-material3 = { module = "androidx.compose.material3:material3" }\n',  # 移除version.ref，由BOM管理
            'androidx-compose-runtime = { module = "androidx.compose.runtime:runtime", version.ref = "androidx-compose" }\n',
            'androidx-compose-runtime-livedata = { module = "androidx.compose.runtime:runtime-livedata", version.ref = "androidx-compose" }\n',
            '# Compose Navigation\n',
            'androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version = "2.7.7" }\n',
            '# Compose Activity\n',
            'androidx-activity-compose = { module = "androidx.activity:activity-compose", version = "1.8.2" }\n',
            '# TV Compose 依赖（使用稳定版0.6.0）\n',
            'androidx-tv-foundation = { module = "androidx.tv:tv-foundation", version.ref = "androidx-tv" }\n',
            'androidx-tv-material = { module = "androidx.tv:tv-material", version.ref = "androidx-tv" }\n',
            '# Lifecycle 依赖\n',
            'androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androidx-lifecycle" }\n',
            'androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "androidx-lifecycle" }\n',
            '# Compose 工具依赖\n',
            'androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling", version.ref = "androidx-compose" }\n',
            'androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest", version.ref = "androidx-compose" }\n',
            'androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4", version.ref = "androidx-compose" }\n',
            '[plugins]\n',
            '# 添加 Compose 插件\n',
            'androidx-compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version = "2.0.21" }\n'
        ]
        lines.extend(append_lines)
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.writelines(lines)
        print(f"✅ 成功修改: {file_path}")
    except Exception as e:
        print(f"❌ 修改 {file_path} 失败: {str(e)}")
        raise

def modify_app_build_gradle_kts(file_path):
    """修改app/build.gradle.kts：替换dependencies块"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_block = """dependencies {
    implementation(project(":app:mobile"))
    implementation(project(":app:tv"))
    implementation(project(":app:shared"))
}"""
        
        new_block = """dependencies {
    implementation(project(":app:mobile"))
    implementation(project(":app:tv"))
    implementation(project(":app:shared"))
    // Compose BOM（统一管理所有Compose版本，避免冲突）
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    // TV Compose 依赖（使用稳定版0.6.0）
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    // Compose 基础依赖
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.livedata)
    // 其他必要的 Compose 依赖
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    // 调试工具
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // 测试依赖
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}"""
        
        if original_block in content:
            content = content.replace(original_block, new_block)
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✅ 成功修改: {file_path}")
        else:
            print(f"⚠️ 未找到目标依赖块: {file_path}")
    except Exception as e:
        print(f"❌ 修改 {file_path} 失败: {str(e)}")
        raise

def modify_tv_build_gradle_kts(file_path):
    """修改app/tv/build.gradle.kts：替换dependencies块"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_block = """dependencies {
    implementation(project(":app:shared"))
}"""
        
        new_block = """dependencies {
    implementation(project(":app:shared"))
    // Compose BOM（统一管理所有Compose版本，避免冲突）
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    // TV Compose 依赖（使用稳定版0.6.0）
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    // Compose 基础依赖
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    // 其他必要的 Compose 依赖
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // 如果您的 TV 模块需要导航
    implementation(libs.androidx.navigation.compose)
    // 调试工具
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // 测试依赖
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}"""
        
        if original_block in content:
            content = content.replace(original_block, new_block)
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✅ 成功修改: {file_path}")
        else:
            print(f"⚠️ 未找到目标依赖块: {file_path}")
    except Exception as e:
        print(f"❌ 修改 {file_path} 失败: {str(e)}")
        raise

def modify_dynamics_screen_kt(file_path):
    """修改DynamicsScreen.kt：
    1. 修复类型不匹配 + 避免重复导入Key
    2. 添加@OptIn注解解决实验性API警告（升级为错误的问题）
    3. 完善LoadingTip的Box修饰符（精准匹配逻辑）
    4. 确保所有插入代码缩进适配原始源码
    """
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        # ===================== 核心新增：添加ExperimentalComposeUiApi导入 =====================
        # 先清理重复的Key导入（如果存在）
        cleaned_lines = []
        key_import = 'import androidx.compose.ui.input.key.Key\n'
        experimental_import = 'import androidx.compose.ui.ExperimentalComposeUiApi\n'
        has_key_import = False
        has_experimental_import = False
        
        for line in lines:
            if line == key_import:
                if not has_key_import:
                    cleaned_lines.append(line)
                    has_key_import = True
            elif line == experimental_import:
                cleaned_lines.append(line)
                has_experimental_import = True
            else:
                cleaned_lines.append(line)
        lines = cleaned_lines
        
        # 若没有ExperimentalComposeUiApi导入，在Key导入上方添加（确保导入顺序合理）
        if not has_experimental_import:
            key_import_index = -1
            for idx, line in enumerate(lines):
                if line == key_import:
                    key_import_index = idx
                    break
            if key_import_index != -1:
                lines.insert(key_import_index, experimental_import)
        
        # 1. import android.content.Intent 后插入focusable
        target1 = 'import android.content.Intent\n'
        insert1 = 'import androidx.compose.foundation.focusable\n'
        for idx, line in enumerate(lines):
            if line == target1:
                lines.insert(idx+1, insert1)
                break
        
        # 2. import androidx.compose.ui.Modifier 后插入4行（不再新增Key导入，因为原文件已有）
        target2 = 'import androidx.compose.ui.Modifier\n'
        insert2 = [
            'import androidx.compose.ui.focus.FocusDirection\n',
            'import androidx.compose.ui.focus.FocusRequester\n',
            'import androidx.compose.ui.focus.focusProperties\n',
            'import androidx.compose.ui.focus.focusRequester\n'
        ]
        for idx, line in enumerate(lines):
            if line == target2:
                # 检查是否已存在这些导入，避免重复
                for insert_line in reversed(insert2):
                    if insert_line not in lines:
                        lines.insert(idx+1, insert_line)
                break
        
        # 3. val scope = rememberCoroutineScope() 后插入3行
        target3 = '    val scope = rememberCoroutineScope()\n'
        insert3 = [
            '    val gridFocusRequester = remember { FocusRequester() }\n',
            '    val gridColumns = 4 // Grid固定列数\n',
            '    val isGridLoadingOrEmpty by remember { derivedStateOf { dynamicViewModel.loadingVideo || dynamicViewModel.dynamicVideoList.isEmpty() } }\n'
        ]
        for idx, line in enumerate(lines):
            if line == target3:
                for l in reversed(insert3):
                    lines.insert(idx+1, l)
                break
        
        # 4. .fillMaxSize() 后插入：先@OptIn注解，再focusProperties代码块（核心修复）
        target4 = '                    .fillMaxSize()\n'
        focus_prop_index = -1
        for idx, line in enumerate(lines):
            if line == target4:
                focus_prop_index = idx
                break
        
        if focus_prop_index != -1:
            # 第一步：插入@OptIn注解（缩进和fillMaxSize一致：20个空格）
            annotation_line = '                    @OptIn(ExperimentalComposeUiApi::class)\n'
            lines.insert(focus_prop_index + 1, annotation_line)
            
            # 第二步：插入focusProperties代码块（修复类型不匹配）
            insert4 = [
                '                    .focusProperties {\n',
                '                        canFocus = true\n',
                '                        enter = { gridFocusRequester }\n',  # 匹配FocusRequester类型
                '                        exit = { gridFocusRequester }\n',  # 匹配FocusRequester类型
                '                    }\n',
                '                    .focusRequester(gridFocusRequester)\n'
            ]
            for l in reversed(insert4):
                lines.insert(focus_prop_index + 2, l)  # 插在注解后
        
        # 5. .onPreviewKeyEvent { 后插入22行（Key.Left/Key.Down处理）
        target5 = '                    .onPreviewKeyEvent {\n'
        insert5 = [
            '                        // 第一层防护：加载/空列表拦截所有方向键\n',
            '                        if (isGridLoadingOrEmpty && it.type == KeyEventType.KeyDown) {\n',
            '                            gridFocusRequester.requestFocus()\n',
            '                            return@onPreviewKeyEvent true\n',
            '                        }\n',
            '                        // 第二层防护：第一列拦截左方向键\n',
            '                        if (it.type == KeyEventType.KeyDown && it.key == Key.Left) {\n',
            '                            val isFirstColumn = currentFocusedIndex >= 0 && (currentFocusedIndex % gridColumns == 0)\n',
            '                            if (isFirstColumn) {\n',
            '                                gridFocusRequester.requestFocus()\n',
            '                                return@onPreviewKeyEvent true\n',
            '                            }\n',
            '                        }\n',
            '                        // 第三层防护：到底部拦截下方向键\n',
            '                        if (it.type == KeyEventType.KeyDown && it.key == Key.Down) {\n',
            '                            val isLastItem = currentFocusedIndex >= dynamicViewModel.dynamicVideoList.size - 1\n',
            '                            if (isLastItem && !dynamicViewModel.videoHasMore) {\n',
            '                                gridFocusRequester.requestFocus()\n',
            '                                return@onPreviewKeyEvent true\n',
            '                            }\n',
            '                        }\n',
            '                        // 保留原有Menu键逻辑\n'
        ]
        for idx, line in enumerate(lines):
            if line == target5:
                for l in reversed(insert5):
                    lines.insert(idx+1, l)
                break
        
        # 6. 匹配LoadingTip的Box中的modifier = Modifier.fillMaxSize()行（精准匹配逻辑）
        # 优化：先找到所有modifier = Modifier.fillMaxSize()行，再搜索上下文是否有LoadingTip()
        loading_tip_modifier_index = -1
        for idx, line in enumerate(lines):
            stripped_line = line.strip()
            if stripped_line in ['modifier = Modifier.fillMaxSize()', 'modifier = Modifier.fillMaxSize(),']:
                # 向上搜索5行，向下搜索5行，检查是否有LoadingTip()
                start_search = max(0, idx - 5)
                end_search = min(len(lines), idx + 5)
                context_lines = lines[start_search:end_search]
                context_text = ''.join(context_lines)
                if 'LoadingTip()' in context_text:
                    loading_tip_modifier_index = idx
                    break
        
        if loading_tip_modifier_index != -1:
            original_line = lines[loading_tip_modifier_index]
            # 提取原有缩进（比如：'                            modifier = ...' → 缩进是28个空格）
            indent = original_line[:original_line.index('modifier')]
            # 移除原有行的逗号（如果有）
            original_modifier = original_line.strip().rstrip(',')
            # 构建新的modifier行（带focusRequester和focusable）
            new_modifier_lines = [
                f'{indent}{original_modifier}\n',
                f'{indent}    .focusRequester(gridFocusRequester)\n',
                f'{indent}    .focusable(){"," if original_line.strip().endswith(",") else ""}\n'
            ]
            # 删除原有行，插入新行
            del lines[loading_tip_modifier_index]
            for l in reversed(new_modifier_lines):
                lines.insert(loading_tip_modifier_index, l)
        else:
            print(f"⚠️ 未找到LoadingTip对应的Box modifier行，跳过该修改（不影响核心编译）")
        
        # 写回修改后的文件
        with open(file_path, 'w', encoding='utf-8') as f:
            f.writelines(lines)
        print(f"✅ 成功修改: {file_path}")
    except Exception as e:
        print(f"❌ 修改 {file_path} 失败: {str(e)}")
        raise

def main():
    if len(sys.argv) != 2:
        print("🚫 用法错误！正确用法：")
        print("python modify_files.py <顶级目录>")
        print("示例：python modify_files.py /home/runner/work/android-ci/android-ci/fantasy-bv-source")
        sys.exit(1)
    
    root_dir = sys.argv[1]
    # 定义需要修改的文件列表（按顺序处理）
    files = [
        (os.path.join(root_dir, "gradle", "libs.versions.toml"), modify_libs_versions_toml),
        (os.path.join(root_dir, "app", "build.gradle.kts"), modify_app_build_gradle_kts),
        (os.path.join(root_dir, "app", "tv", "build.gradle.kts"), modify_tv_build_gradle_kts),
        (os.path.join(root_dir, "app", "tv", "src", "main", "kotlin", "dev", "aaa1115910", "bv", "tv", "screens", "main", "home", "DynamicsScreen.kt"), modify_dynamics_screen_kt)
    ]
    
    # 检查所有文件是否存在
    for file_path, _ in files:
        if not os.path.exists(file_path):
            print(f"🚫 文件不存在：{file_path}")
            sys.exit(1)
    
    # 依次修改所有文件
    for file_path, modify_func in files:
        modify_func(file_path)
    
    print("\n🎉 所有文件修改完成！CI编译前的准备已全部完成。")

if __name__ == "__main__":
    main()