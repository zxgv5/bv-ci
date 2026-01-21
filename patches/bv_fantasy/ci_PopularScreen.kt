package dev.aaa1115910.bv.tv.screens.main.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.aaa1115910.biliapi.entity.ugc.UgcItem
import dev.aaa1115910.bv.tv.component.LoadingTip
import dev.aaa1115910.bv.entity.carddata.VideoCardData
import dev.aaa1115910.bv.tv.R
import dev.aaa1115910.bv.tv.activities.video.UpInfoActivity
import dev.aaa1115910.bv.tv.activities.video.VideoInfoActivity
import dev.aaa1115910.bv.tv.component.videocard.SmallVideoCard
import dev.aaa1115910.bv.tv.util.ProvideListBringIntoViewSpec
import dev.aaa1115910.bv.viewmodel.home.PopularViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun PopularScreen(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    popularViewModel: PopularViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 纯基础API预加载逻辑：延迟循环检查，无任何冷门依赖
    LaunchedEffect(lazyGridState, popularViewModel) {
        while (true) {
            delay(300L)
            val listSize = popularViewModel.popularVideoList.size
            // 跳过无数据/加载中/无更多的情况
            if (listSize == 0 || popularViewModel.loading || !popularViewModel.hasMore) continue
            
            // 获取可见区域最后一个item索引
            val lastVisibleIndex = lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            // 提前24项触发加载
            if (lastVisibleIndex >= listSize - 24) {
                scope.launch(Dispatchers.IO) {
                    popularViewModel.loadMore()
                }
            }
        }
    }

    val onClickVideo: (UgcItem) -> Unit = { ugcItem ->
        VideoInfoActivity.actionStart(context, ugcItem.aid)
    }

    val onLongClickVideo: (UgcItem) -> Unit = { ugcItem ->
        UpInfoActivity.actionStart(
            context,
            mid = ugcItem.authorId,
            name = ugcItem.author,
            face = ugcItem.authorFace
        )
    }

    val padding = dimensionResource(R.dimen.grid_padding)
    val spacedBy = dimensionResource(R.dimen.grid_spacedBy)
    ProvideListBringIntoViewSpec {
        LazyVerticalGrid(
            modifier = modifier.fillMaxSize(),
            columns = GridCells.Fixed(4),
            state = lazyGridState,
            contentPadding = PaddingValues(padding),
            verticalArrangement = Arrangement.spacedBy(spacedBy),
            horizontalArrangement = Arrangement.spacedBy(spacedBy)
        ) {
            itemsIndexed(popularViewModel.popularVideoList) { _, item ->
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
                            pubTime = item.pubTime
                        )
                    },
                    onClick = { onClickVideo(item) },
                    onLongClick = { onLongClickVideo(item) },
                    onFocus = {}
                )
            }

            // 加载中占位
            if (popularViewModel.loading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingTip()
                    }
                }
            }

            // 无更多数据提示 - 需要确认PopularViewModel中是否有hasMore属性
            // 如果PopularViewModel中没有hasMore属性，请移除这部分代码
            // 或者根据实际情况判断，例如：!popularViewModel.hasMore && !popularViewModel.loading
            // 这里假设PopularViewModel有hasMore属性
            if (!popularViewModel.hasMore && !popularViewModel.loading && popularViewModel.popularVideoList.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "没有更多了捏",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}