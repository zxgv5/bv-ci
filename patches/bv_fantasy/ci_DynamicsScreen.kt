package dev.aaa1115910.bv.tv.screens.main.home

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.GridCells
import androidx.tv.foundation.lazy.grid.LazyVerticalGrid
import androidx.tv.foundation.lazy.grid.rememberLazyVerticalGridState
import androidx.tv.material3.CircularProgressIndicator
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.aaa1115910.bv.tv.component.videocard.SmallVideoCard
import dev.aaa1115910.bv.tv.viewmodel.DynamicViewModel
import dev.aaa1115910.bv.util.ModifierExtends.focusedBorder
import dev.aaa1115910.bv.util.ModifierExtends.focusedScale
import org.koin.androidx.compose.koinViewModel

/**
 * TV端动态页面
 * 修复焦点左移溢出问题：焦点边界防护 + 焦点-加载时序对齐 + 索引同步
 */
@Composable
fun DynamicsScreen(
    modifier: Modifier = Modifier,
    onVideoClick: (String) -> Unit, // 视频点击回调
    onUpSpaceClick: (String) -> Unit, // UP主空间点击回调
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val dynamicViewModel: DynamicViewModel = koinViewModel()
    val dynamicVideoList by dynamicViewModel.dynamicVideoList.collectAsState()
    val isLoading by dynamicViewModel.isLoading.collectAsState()
    val lazyGridState = rememberLazyVerticalGridState()
    val scope = rememberCoroutineScope()
    
    // 记录最后一个可聚焦的视频卡片索引
    val lastFocusableIndex = remember { mutableIntStateOf(0) }
    // 当前聚焦的索引
    val currentFocusedIndex by dynamicViewModel.currentFocusedIndex.collectAsState()

    // 1. 焦点边界防护：阻止焦点溢出到左侧边栏
    androidx.tv.foundation.focus.FocusBoundary(
        focusDirection = { direction ->
            when (direction) {
                // 下方向键：检查目标项是否存在，不存在则锁定焦点
                FocusDirection.Down -> {
                    val targetIndex = currentFocusedIndex + 4 // 4列布局，同列下一行偏移量
                    if (targetIndex < dynamicVideoList.size && lazyGridState.isItemVisible(targetIndex)) {
                        FocusDirection.Down
                    } else {
                        FocusDirection.Current // 锁定焦点在当前最后一个有效项
                    }
                }
                // 左方向键：仅第一列允许左移离开Grid
                FocusDirection.Left -> {
                    if (currentFocusedIndex % 4 == 0) FocusDirection.Left else FocusDirection.Current
                }
                // 其他方向保持原有逻辑
                else -> direction
            }
        }
    ) {
        LazyVerticalGrid(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                // 2. 拦截快速下滚的无效焦点移动
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionDown) {
                        val targetIndex = currentFocusedIndex + 4
                        // 目标项未加载/未渲染 → 消费事件，阻止无效焦点移动
                        if (targetIndex >= dynamicVideoList.size || !lazyGridState.isItemVisible(targetIndex)) {
                            return@onKeyEvent true
                        }
                    }
                    false
                },
            state = lazyGridState,
            columns = GridCells.Fixed(4), // 4列布局（适配TV端）
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = androidx.tv.foundation.lazy.grid.Arrangement.spacedBy(24.dp),
            verticalArrangement = androidx.tv.foundation.lazy.grid.Arrangement.spacedBy(16.dp)
        ) {
            // 动态视频列表项
            itemsIndexed(dynamicVideoList) { index, video ->
                SmallVideoCard(
                    modifier = Modifier
                        .focusedBorder(animate = true) // 项目已有焦点边框扩展
                        .focusedScale(scale = 0.9f) // 项目已有焦点缩放扩展
                        .onDelayFocusChanged(delayTime = 100L) { focusState ->
                            if (focusState.hasFocus) {
                                dynamicViewModel.updateFocusedIndex(index) // 同步焦点索引
                                lastFocusableIndex.intValue = index // 更新最后可聚焦索引
                            }
                        },
                    video = video,
                    onVideoClick = { onVideoClick(video.bvid) },
                    onLongClick = { onUpSpaceClick(video.mid) }, // 长按确认键进UP空间（项目需求）
                    onMenuKeyClick = { dynamicViewModel.openFollowedUpList() } // 菜单键打开已关注UP列表
                )
            }

            // 3. 加载中占位项：保证Grid布局稳定，避免无项渲染导致焦点异常
            if (isLoading) {
                items(4) { // 补全一行占位（4列）
                    VideoCardPlaceholder()
                }
            }
        }
    }

    // 4. 提前触发加载：阈值从12改为8，预留加载时间
    LaunchedEffect(lastFocusableIndex.intValue, dynamicVideoList.size) {
        val shouldLoadMore = lastFocusableIndex.intValue + 8 > dynamicVideoList.size && !isLoading
        if (shouldLoadMore) {
            dynamicViewModel.loadMoreVideo()
        }
    }

    // 5. 监听Grid渲染项变化，同步最后可聚焦索引（解决索引滞后问题）
    LaunchedEffect(lazyGridState.layoutInfo) {
        val visibleItems = lazyGridState.layoutInfo.visibleItemsInfo
        if (visibleItems.isNotEmpty()) {
            val lastRenderedIndex = visibleItems.maxOf { it.index }
            if (lastRenderedIndex > lastFocusableIndex.intValue) {
                lastFocusableIndex.intValue = lastRenderedIndex
            }
        }
    }
}

/**
 * 视频卡片占位项（加载中显示）
 */
@Composable
private fun VideoCardPlaceholder() {
    Box(
        modifier = Modifier
            .size(240.dp, 180.dp) // 与实际视频卡片尺寸一致
            .focusable(false), // 占位项不可聚焦
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                CircularProgressIndicator()
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "加载中...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * 扩展方法：检查LazyVerticalGrid中指定索引的项是否已渲染（可视/预加载）
 */
fun androidx.tv.foundation.lazy.grid.LazyVerticalGridState.isItemVisible(index: Int): Boolean {
    val layoutInfo = this.layoutInfo
    return layoutInfo.visibleItemsInfo.any { it.index == index } ||
            index <= layoutInfo.totalItemsCount - 1
}