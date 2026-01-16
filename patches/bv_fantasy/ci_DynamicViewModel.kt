package dev.aaa1115910.bv.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.aaa1115910.bv.data.entity.DynamicVideoItem
import dev.aaa1115910.bv.data.repository.DynamicRepository
import dev.aaa1115910.bv.util.Event
import dev.aaa1115910.bv.util.MutableStateFlow
import dev.aaa1115910.bv.util.StateFlow
import dev.aaa1115910.bv.util.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.inject

/**
 * 动态页面ViewModel
 * 优化：预加载、焦点索引同步、加载状态管理
 */
class DynamicViewModel : ViewModel() {
    // 依赖注入：动态数据仓库
    private val dynamicRepository: DynamicRepository by inject()

    // 动态视频列表
    private val _dynamicVideoList = MutableStateFlow<List<DynamicVideoItem>>(emptyList())
    val dynamicVideoList: StateFlow<List<DynamicVideoItem>> = _dynamicVideoList.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 当前聚焦的索引
    private val _currentFocusedIndex = MutableStateFlow(0)
    val currentFocusedIndex: StateFlow<Int> = _currentFocusedIndex.asStateFlow()

    // 打开已关注UP列表的事件（通知UI层）
    private val _openFollowedUpListEvent = MutableStateFlow<Event<Unit>>(Event.Uninitialized)
    val openFollowedUpListEvent: StateFlow<Event<Unit>> = _openFollowedUpListEvent.asStateFlow()

    // 分页参数
    private var currentPage = 1
    private val pageSize = 20 // 单页加载20条

    init {
        // 初始化预加载2页数据（减少快速滚动加载等待）
        viewModelScope.launch(Dispatchers.IO) {
            loadMoreVideo(preLoadPages = 2)
        }
    }

    /**
     * 加载更多动态视频
     * @param preLoadPages 预加载页数（默认1页）
     */
    fun loadMoreVideo(preLoadPages: Int = 1) {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                // 按预加载页数加载多页数据
                val totalLoadSize = pageSize * preLoadPages
                val newData = dynamicRepository.getDynamicVideos(
                    page = currentPage,
                    pageSize = totalLoadSize
                )
                // 追加数据（避免覆盖原有列表）
                _dynamicVideoList.update { it + newData }
                currentPage += preLoadPages
            }.onFailure {
                // 异常处理（可根据项目补充日志/Toast）
                it.printStackTrace()
            }.finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 更新当前聚焦的索引
     */
    fun updateFocusedIndex(index: Int) {
        _currentFocusedIndex.value = index
    }

    /**
     * 触发打开已关注UP列表事件
     */
    fun openFollowedUpList() {
        _openFollowedUpListEvent.value = Event.Success(Unit)
    }
}