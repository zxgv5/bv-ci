package dev.aaa1115910.bv.tv.screens.main.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.aaa1115910.biliapi.entity.user.DynamicVideo
import dev.aaa1115910.bv.tv.component.LoadingTip
import dev.aaa1115910.bv.entity.carddata.VideoCardData
import dev.aaa1115910.bv.entity.proxy.ProxyArea
import dev.aaa1115910.bv.tv.R
import dev.aaa1115910.bv.tv.activities.user.FollowActivity
import dev.aaa1115910.bv.tv.activities.video.UpInfoActivity
import dev.aaa1115910.bv.tv.activities.video.VideoInfoActivity
import dev.aaa1115910.bv.tv.component.videocard.SmallVideoCard
import dev.aaa1115910.bv.tv.util.ProvideListBringIntoViewSpec
import dev.aaa1115910.bv.viewmodel.home.DynamicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun DynamicsScreen(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    dynamicViewModel: DynamicViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentFocusedIndex by remember { mutableIntStateOf(-1) }
    val shouldLoadMore by remember {
        derivedStateOf { dynamicViewModel.dynamicVideoList.isNotEmpty() && currentFocusedIndex + 12 > dynamicViewModel.dynamicVideoList.size }
    }
    val showTip by remember {
        derivedStateOf { dynamicViewModel.dynamicVideoList.isNotEmpty() && currentFocusedIndex >= 0 }
    }
    // 计算当前焦点所在的行（每行4列）
    val currentRow by remember {
        derivedStateOf { if (currentFocusedIndex >= 0) currentFocusedIndex / 4 else -1 }
    }
    // 计算总行数
    val totalRows by remember {
        derivedStateOf { (dynamicViewModel.dynamicVideoList.size + 3) / 4 } // 向上取整
    }
    // 判断是否在最下面一行
    val isAtBottomRow by remember {
        derivedStateOf { currentRow >= 0 && currentRow >= totalRows - 1 }
    }
    // 判断是否接近列表末尾（最后3行），用于在加载卡顿时防止焦点移出
    val isNearBottom by remember {
        derivedStateOf { 
            currentRow >= 0 && totalRows > 0 && currentRow >= totalRows - 3
        }
    }
    // 判断是否接近列表末尾（最后12个项，约3行），用于在加载卡顿时防止焦点移出
    val isNearBottomByIndex by remember {
        derivedStateOf {
            currentFocusedIndex >= 0 && dynamicViewModel.dynamicVideoList.isNotEmpty() &&
            currentFocusedIndex >= dynamicViewModel.dynamicVideoList.size - 12
        }
    }
    // 判断是否应该阻止焦点向下移动：当正在加载且接近列表末尾时
    val shouldBlockDownKey by remember {
        derivedStateOf {
            dynamicViewModel.loadingVideo && (isNearBottom || isNearBottomByIndex)
        }
    }

    val onClickVideo: (DynamicVideo) -> Unit = { dynamic ->
        VideoInfoActivity.actionStart(
            context = context,
            aid = dynamic.aid,
            proxyArea = ProxyArea.checkProxyArea(dynamic.title)
        )
    }

    val onLongClickVideo: (DynamicVideo) -> Unit = { dynamic ->
        UpInfoActivity.actionStart(
            context,
            mid = dynamic.authorId,
            name = dynamic.author,
            face = dynamic.authorFace
        )
    }

    //不能直接使用 LaunchedEffect(currentFocusedIndex)，会导致整个页面重组
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            scope.launch(Dispatchers.IO) {
                dynamicViewModel.loadMoreVideo()
            }
        }
    }

    if (dynamicViewModel.isLogin) {
        val padding = dimensionResource(R.dimen.grid_padding)
        val spacedBy = dimensionResource(R.dimen.grid_spacedBy)
        if (showTip) {
            Text(
                modifier = Modifier.fillMaxWidth().offset(x = (-20).dp, y = (-8).dp),
                text = stringResource(R.string.entry_follow_screen),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.End
            )
        }
        ProvideListBringIntoViewSpec {
            LazyVerticalGrid(
                modifier = modifier
                    .fillMaxSize()
                    .onFocusChanged{
                        if (!it.isFocused) {
                            currentFocusedIndex = -1
                        }
                    }
                    .onPreviewKeyEvent { event ->
                        // 处理菜单键
                        if(event.type == KeyEventType.KeyUp && event.key == Key.Menu) {
                            context.startActivity(Intent(context, FollowActivity::class.java))
                            return@onPreviewKeyEvent true
                        }
                        
                        // 处理下方向键：防止在加载卡顿时焦点移出页面
                        // 当正在加载且焦点接近列表末尾（最后3行或最后12个项）时，阻止焦点向下移动
                        // 这样可以防止当网络加载慢或无法加载时，焦点因为找不到下一个可聚焦项目而向左移动并移出页面
                        if(event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown) {
                            // 情况1：正在加载且接近列表末尾，阻止向下移动
                            if (shouldBlockDownKey) {
                                return@onPreviewKeyEvent true
                            }
                            // 情况2：在最下面一行且没有更多数据，阻止向下移动
                            if (isAtBottomRow && !dynamicViewModel.videoHasMore) {
                                return@onPreviewKeyEvent true
                            }
                        }
                        
                        false
                    },
                columns = GridCells.Fixed(4),
                state = lazyGridState,
                contentPadding = PaddingValues(padding),
                verticalArrangement = Arrangement.spacedBy(spacedBy),
                horizontalArrangement = Arrangement.spacedBy(spacedBy)
            ) {
                itemsIndexed(dynamicViewModel.dynamicVideoList) { index, item ->
                    SmallVideoCard(
                        data = remember(item.aid) {
                            VideoCardData(
                                avid = item.aid,
                                title = item.title,
                                cover = item.cover,
                                play = item.play,
                                danmaku = item.danmaku,
                                upName = item.author,
                                time = item.duration * 1000L,
                                pubTime = item.pubTime,
                                isChargingArc = item.isChargingArc,
                                badgeText = item.chargingArcBadge
                            )
                        },
                        onClick = { onClickVideo(item) },
                        onLongClick = {onLongClickVideo(item) },
                        onFocus = { currentFocusedIndex = index }
                    )
                }

                if (dynamicViewModel.loadingVideo) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingTip()
                        }
                    }
                }

                if (!dynamicViewModel.videoHasMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "没有更多了捏",
                            color = Color.White
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "请先登录")
        }
    }
}
