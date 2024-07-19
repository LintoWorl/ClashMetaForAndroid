package app.hw.network.handler

import app.hw.network.api.ResponseData
import com.github.kr328.clash.common.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @Time : created on 2024/4/23 17:22
 * @Description :封装业务层调用服务端接口的请求、解析、异常处理逻辑
 */

object RequestHandler {

    fun <T> request(
        block: suspend () -> ResponseData<T>,
        onSucc: (data: T) -> Unit,
        onFail: (code: Int, msg: String) -> Unit,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        scope.launch {
            runCatching { block() }
                .onSuccess {
                    launch(Dispatchers.Main) {
                        //parseData(it, onSucc, onFail)
                        onSucc(it.data!!)
                    }
                }
                .onFailure {
                    val ex = ExceptionHandler().handleException(it)
                    Log.e("request fail, errCode:${ex.code}, errMsg:${ex.message}")
                    launch(Dispatchers.Main) { onFail(ex.code, ex.message) }
                }
        }
    }

    private fun <T> parseData(
        response: ResponseData<T>,
        onSucc: (data: T) -> Unit,
        onFail: (code: Int, msg: String) -> Unit
    ) {
        val exceptionHandler = ExceptionHandler()
        when (response.code) {
            200 -> {
                if (response.data != null) {
                    try {
                        onSucc(response.data!!)
                    } catch (e: Exception) {
                        val ex = exceptionHandler.handleException(e)
                        onFail(ex.code, ex.message)
                    }
                } else {
                    onFail(-1, "response data is null.")
                }
            }

            else -> {
                //TODO 需根据业务补写相关逻辑
                if (exceptionHandler.tokenExpired(response.code)) {
                    //用户登录token过期，统一处理跳到登录页面
                } else {

                }
            }
        }
    }

}