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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var currentFocusedIndex by remember { mutableIntStateOf(-1) }
    val gridColumns = 4 //与GridCells.Fixed(4)保持一致

    //优化加载触发条件：提前6项开始加载，避免列表项缺失
    val shouldLoadMore by remember {
        derivedStateOf {
            dynamicViewModel.dynamicVideoList.isNotEmpty() &&
                    currentFocusedIndex != -1 &&
                    currentFocusedIndex + 6 > dynamicViewModel.dynamicVideoList.size &&
                    !dynamicViewModel.loadingVideo
        }
    }

    val showTip by remember {
        derivedStateOf { dynamicViewModel.dynamicVideoList.isNotEmpty() && currentFocusedIndex >= 0 }
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

    //预加载触发：更早、更及时补充列表项
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            scope.launch(Dispatchers.IO) {
                dynamicViewModel.loadMoreVideo()
            }
        }
    }

    //初始预加载：进入页面就加载足够多的项
    LaunchedEffect(Unit) {
        if (dynamicViewModel.isLogin && dynamicViewModel.dynamicVideoList.size < 20) {
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
                    .onFocusChanged {
                        if (!it.isFocused) {
                            currentFocusedIndex = -1
                        }
                    }
                    .onPreviewKeyEvent { event ->
                        //拦截下方向键：无下一项且加载中时，阻止焦点移动
                        if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown) {
                            val currentListSize = dynamicViewModel.dynamicVideoList.size
                            if (currentFocusedIndex != -1 && currentListSize > 0) {
                                val nextIndex = currentFocusedIndex + gridColumns
                                //判断是否有下一项，或是否正在加载
                                if (nextIndex >= currentListSize && dynamicViewModel.loadingVideo) {
                                    //无下一项且加载中，阻止焦点移动
                                    return@onPreviewKeyEvent true
                                }
                            }
                        }
                        //Menu键跳转逻辑保留
                        if (event.type == KeyEventType.KeyUp && event.key == Key.Menu) {
                            context.startActivity(Intent(context, FollowActivity::class.java))
                            return@onPreviewKeyEvent true
                        }
                        false
                    },
                columns = GridCells.Fixed(gridColumns),
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
                        onLongClick = { onLongClickVideo(item) },
                        onFocus = {
                            //强制更新焦点索引，避免快速操作时滞后
                            currentFocusedIndex = index
                        }
                    )
                }

                //加载提示
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

                //无更多数据提示
                if (!dynamicViewModel.videoHasMore && !dynamicViewModel.loadingVideo) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                            text = "没有更多了捏",
                            color = Color.White,
                            textAlign = TextAlign.Center
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
