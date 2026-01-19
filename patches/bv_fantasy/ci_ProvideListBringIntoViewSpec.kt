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
 * 修复焦点跳转问题的 BringIntoViewSpec
 * 将焦点项定位在屏幕上方30%处
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
        ): Float {
            // 简化算法：将焦点项定位在屏幕上方30%处
            val targetPosition = containerSize * 0.3f
            
            // 计算项的可见范围
            val itemTop = offset
            val itemBottom = offset + size
            
            // 如果项已经可见，不需要滚动
            val visibleTop = paddingPx
            val visibleBottom = containerSize - paddingPx
            
            if (itemTop >= visibleTop && itemBottom <= visibleBottom) {
                return 0f
            }
            
            // 计算滚动距离，使项的上边缘对齐目标位置
            return offset - targetPosition
        }
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