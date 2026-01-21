#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
注释日志工具
用于注释掉Kotlin项目中特定的日志相关代码
"""

import os
import re
import sys
from pathlib import Path

def should_comment_line(line):
    """
    判断一行是否应该被注释
    """
    # 如果行已经被注释，则不需要再次注释
    if line.strip().startswith('//'):
        return False
    
    # 检查是否包含需要注释的特定内容
    patterns = [
        # 导入语句
        r'import\s+io\.github\.oshai\.kotlinlogging\.KotlinLogging',
        r'import\s+dev\.aaa1115910\.bv\.util\.fInfo',
        
        # KotlinLogging.logger {}
        r'KotlinLogging\.logger\s*\{',
        
        # logger函数调用（单行）
        r'logger\s*\(\s*["\']BvVideoPlayer["\']\s*\)',
        
        # androidLogger（全字匹配）
        r'\bandroidLogger\b',
    ]
    
    # logger的各种方法调用（处理单行情况）
    logger_methods = [
        'info', 'fInfo', 'warn', 'fWarn', 'error', 
        'fError', 'exception', 'fException', 'debug', 'fDebug'
    ]
    
    # 检查基础模式
    for pattern in patterns:
        if re.search(pattern, line):
            return True
    
    # 检查logger方法调用
    for method in logger_methods:
        # 匹配 logger.method(...) 形式
        pattern = fr'logger\.{method}\s*\('
        if re.search(pattern, line):
            return True
    
    return False

def process_file(filepath):
    """
    处理单个文件
    """
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        modified = False
        new_lines = []
        
        i = 0
        while i < len(lines):
            line = lines[i]
            
            # 检查当前行是否需要注释
            if should_comment_line(line):
                # 处理可能的多行函数调用
                start_index = i
                end_index = i
                
                # 检查当前行是否包含函数调用的开始
                if re.search(r'logger\.\w+\s*\(', line):
                    # 查找函数调用的结束
                    paren_count = line.count('(') - line.count(')')
                    
                    # 如果当前行没有结束函数调用，继续查找后续行
                    j = i + 1
                    while j < len(lines) and paren_count > 0:
                        paren_count += lines[j].count('(')
                        paren_count -= lines[j].count(')')
                        end_index = j
                        j += 1
                
                # 注释从start_index到end_index的所有行
                for idx in range(start_index, end_index + 1):
                    comment_line = lines[idx]
                    if not comment_line.strip().startswith('//'):
                        # 保留缩进
                        match = re.match(r'^(\s*)', comment_line)
                        if match:
                            indent = match.group(1)
                            rest = comment_line[len(indent):]
                            new_lines.append(indent + '//' + rest)
                        else:
                            new_lines.append('//' + comment_line)
                        modified = True
                    else:
                        new_lines.append(comment_line)
                
                i = end_index + 1
            else:
                new_lines.append(line)
                i += 1
        
        # 如果有修改，写回文件
        if modified:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)
            return True
        
        return False
    
    except Exception as e:
        print(f"处理文件 {filepath} 时出错: {e}")
        return False

def main():
    """
    主函数
    """
    if len(sys.argv) != 2:
        print("用法: python comment_logger.py <项目根目录>")
        print("示例: python comment_logger.py /path/to/fantasy-bv-source")
        sys.exit(1)
    
    root_dir = sys.argv[1]
    
    if not os.path.isdir(root_dir):
        print(f"错误: 目录不存在: {root_dir}")
        sys.exit(1)
    
    print(f"开始处理目录: {root_dir}")
    print("搜索并注释日志相关代码...")
    
    # 查找所有.kt文件
    processed_count = 0
    error_count = 0
    
    for root, dirs, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.kt'):
                filepath = os.path.join(root, file)
                try:
                    if process_file(filepath):
                        processed_count += 1
                        print(f"已处理: {filepath}")
                except Exception as e:
                    error_count += 1
                    print(f"处理失败: {filepath} - {e}")
    
    print(f"\n处理完成!")
    print(f"成功处理文件数: {processed_count}")
    print(f"处理失败文件数: {error_count}")

if __name__ == "__main__":
    main()