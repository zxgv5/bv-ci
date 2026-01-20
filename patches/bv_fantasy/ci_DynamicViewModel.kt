package dev.aaa1115910.bv.viewmodel.home
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.aaa1115910.biliapi.entity.user.DynamicVideo
import dev.aaa1115910.biliapi.http.entity.AuthFailureException
import dev.aaa1115910.biliapi.repositories.UserRepository
import dev.aaa1115910.bv.BVApp
import dev.aaa1115910.bv.BuildConfig
import dev.aaa1115910.bv.R
import dev.aaa1115910.bv.util.Prefs
import dev.aaa1115910.bv.util.addAllWithMainContext
import dev.aaa1115910.bv.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import dev.aaa1115910.bv.repository.UserRepository as BvUserRepository

@KoinViewModel
class DynamicViewModel(
    private val bvUserRepository: BvUserRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    // 仅保留动态视频列表，删除冗余的 dynamicAllList
    val dynamicVideoList = mutableStateListOf<DynamicVideo>()
    private var currentPage = 0
    // 规范状态：private set 防止多线程乱改，避免重复加载
    var loading by mutableStateOf(false)
        private set
    var hasMore by mutableStateOf(true)
        private set
    private var historyOffset: String? = null
    private var updateBaseline: String? = null
    val isLogin get() = bvUserRepository.isLogin

    // 统一加载方法，删除冗余的 loadMoreAll/loadAllData
    suspend fun loadMore() {
        if (!loading && hasMore && isLogin) {
            loadData()
        }
    }

    private suspend fun loadData() {
        loading = true
        val nextPage = currentPage + 1
        // 最多重试2次，解决单次网络波动导致的加载卡住（无日志）
        repeat(2) { retryCount ->
            runCatching {
                val data = userRepository.getDynamicVideos(
                    page = nextPage,
                    offset = historyOffset.orEmpty(),
                    updateBaseline = updateBaseline.orEmpty(),
                    preferApiType = Prefs.apiType
                )
                // 成功加载：更新状态并退出重试
                currentPage = nextPage
                dynamicVideoList.addAllWithMainContext(data.videos)
                historyOffset = data.historyOffset
                updateBaseline = data.updateBaseline
                hasMore = data.hasMore
                return@repeat
            }.onFailure { e ->
                // 最后一次重试失败才提示，减少Toast干扰
                if (retryCount == 1) {
                    withContext(Dispatchers.Main) {
                        when (e) {
                            is AuthFailureException -> {
                                BVApp.context.getString(R.string.exception_auth_failure).toast(BVApp.context)
                                if (!BuildConfig.DEBUG) bvUserRepository.logout()
                            }
                            else -> "加载动态失败: ${e.localizedMessage}".toast(BVApp.context)
                        }
                    }
                } else {
                    delay(800) // 缩短重试间隔，提升响应速度
                }
            }
        }
        loading = false
    }

    // 简化清空逻辑，无冗余操作
    fun clearData() {
        dynamicVideoList.clear()
        currentPage = 0
        loading = false
        hasMore = true
        historyOffset = null
        updateBaseline = null
    }
}
