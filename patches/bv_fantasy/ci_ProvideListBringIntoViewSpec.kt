package dev.aaa1115910.bv.tv.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Provides a [BringIntoViewSpec] that calculates the scroll offset for a child item in a LazyList
 * with intelligent positioning logic.
 *
 * The positioning logic:
 * 1. If the focused element is fully visible, don't scroll
 * 2. If the focused element is in the upper, align its top edge with container top
 * 3. If the focused element is in the lower, align its bottom edge with container bottom
 *
 * @param padding 容器上下左右预留的内边距。单位是dp
 *     注意：延迟列表默认只组合可见项，必须留边距露出一点点下一行用来确保将要获得焦点的项已被组合，否则下移的时候焦点会选中下一行的第一个，上移的时候焦点会选中上一行的最后一个（焦点乱跳的问题）
 *     另外，本应用列表用的视频卡片组件有发光效果，不留边距会没显示不全。
 * @param content 包含在 LazyList 中的内容
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProvideListBringIntoViewSpec(
    padding: Dp = 24.dp,
    content: @Composable () -> Unit,
) {
    val paddingPx = LocalDensity.current.run { padding.toPx() }
    val bringIntoViewSpec = object : BringIntoViewSpec {
        override fun calculateScrollDistance(
            offset: Float,
            size: Float,
            containerSize: Float
        ): Float = calculateScrollDistanceMod(
            offset = offset,
            size = size,
            containerSize = containerSize,
            padding = paddingPx
        )
    }
    CompositionLocalProvider(
        LocalBringIntoViewSpec provides bringIntoViewSpec,
        content = content,
    )
}

private fun calculateScrollDistanceMod(
    offset: Float,
    size: Float,
    containerSize: Float,
    padding: Float = 90f
): Float {
    // 简化算法：将焦点项定位在屏幕上方25%处
    val targetPosition = containerSize * 0.25f
    
    // 计算项的上边缘和下边缘（考虑padding）
    val topEdge = offset - padding
    val bottomEdge = offset + size + padding
    
    // 如果项完全可见（包括padding区域），不需要滚动
    if (topEdge >= 0 && bottomEdge <= containerSize) {
        return 0f
    }
    
    // 计算需要滚动的距离，使项的上边缘对齐目标位置
    return offset - targetPosition
}