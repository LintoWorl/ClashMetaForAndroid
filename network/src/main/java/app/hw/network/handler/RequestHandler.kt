package app.hw.network.handler

import android.util.Log
import app.hw.network.api.ResponseData
import app.hw.network.contant.Constant.TAG_EXP
import app.hw.network.contant.Constant.TAG_RES
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
                    Log.i(TAG_RES, "request success, response:${it}, data:${it.data}")
                    launch(Dispatchers.Main) {
                        it.data?.apply(onSucc)
                    }
                }
                .onFailure {
                    Log.e(TAG_EXP, "request fail, throwable:${it.message}")
                    val ex = ExceptionHandler().handleException(it)
                    ex ?: return@onFailure
                    Log.e(TAG_EXP, "request fail, errCode:${ex.code}, errMsg:${ex.message}")
                    launch(Dispatchers.Main) { onFail(ex.code, ex.message) }
                }
        }
    }

}