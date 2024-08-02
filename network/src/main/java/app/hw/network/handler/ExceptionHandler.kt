package app.hw.network.handler

import android.net.ParseException
import app.hw.network.handler.ErrorType.ERROR_CONN_TIMEOUT
import app.hw.network.handler.ErrorType.ERROR_HTTP_PROTOCOL
import app.hw.network.handler.ErrorType.ERROR_NETWORK_FAIL
import app.hw.network.handler.ErrorType.ERROR_PARSE_FAIL
import app.hw.network.handler.ErrorType.ERROR_SSL_EXCEPTION
import app.hw.network.handler.ErrorType.ERROR_UNKNOWN
import app.hw.network.handler.ErrorType.SERVER_STATE_BAD_GATEWAY
import app.hw.network.handler.ErrorType.SERVER_STATE_FORBIDDEN
import app.hw.network.handler.ErrorType.SERVER_STATE_GATEWAY_TIMEOUT
import app.hw.network.handler.ErrorType.SERVER_STATE_INTERNAL_ERROR
import app.hw.network.handler.ErrorType.SERVER_STATE_REQUEST_TIMEOUT
import app.hw.network.handler.ErrorType.SERVER_STATE_UNAUTHORIZED
import app.hw.network.handler.ErrorType.SERVER_STATE_UNAVAILABLE
import app.hw.network.handler.ErrorType.SERVER_STATE_UNFOUND
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.util.decodeUnicode
import com.google.gson.JsonParseException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

/**
 * @Time : created on 2024/4/22 13:40
 * @Description :网络请求及数据解析中的异常处理
 */
class ExceptionHandler {

    fun handleException(throwable: Throwable?): ResponseThrowable? {
        throwable ?: return null
        //返回时抛出异常
        val responseThrowable: ResponseThrowable
        return when (throwable) {
            is HttpException -> {
                responseThrowable = ResponseThrowable(throwable, ERROR_HTTP_PROTOCOL)
                val body = throwable.response()?.errorBody()?.string()
                    ?: "hi bro, there's something wrong."
                val regularStr = decodeUnicode(body)
                Logger.d("the Error body is:${regularStr}")
                try {
                    val json = JSONObject(regularStr)
                    if (json.has("errors")) {
                        val errJson = json.optJSONObject("errors")
                        errJson?.apply {
                            val content = toString().trimStart { it == '{' }.trimEnd { it == '}' }
                            responseThrowable.message = content.replace(",", "\n")
                        }
                    } else {
                        responseThrowable.message = json.optString("message")
                    }
                } catch (e: Exception) {
                    responseThrowable.message = regularStr
                }

                responseThrowable.code = throwable.code()
                responseThrowable
            }

            is ServerError -> {
                //服务器异常
                val resultException: ServerError = throwable
                responseThrowable = ResponseThrowable(resultException, resultException.code)
                responseThrowable.message = resultException.message
                responseThrowable
            }

            is JsonParseException, is JSONException, is ParseException -> {
                responseThrowable = ResponseThrowable(throwable, ERROR_PARSE_FAIL)
                responseThrowable.message = "parse data failed."
                responseThrowable
            }

            is ConnectException -> {
                responseThrowable = ResponseThrowable(throwable, ERROR_NETWORK_FAIL)
                responseThrowable.message = "connect failed."
                responseThrowable
            }

            is SSLHandshakeException -> {
                responseThrowable = ResponseThrowable(throwable, ERROR_SSL_EXCEPTION)
                responseThrowable.message = "Certificate verification failed."
                responseThrowable
            }

            is SocketTimeoutException -> {
                responseThrowable = ResponseThrowable(throwable, ERROR_CONN_TIMEOUT)
                responseThrowable.message = "connect timeout."
                responseThrowable
            }

            else -> {
                responseThrowable = ResponseThrowable(throwable, ERROR_UNKNOWN)
                responseThrowable.message = "unknown error:$throwable"
                responseThrowable
            }
        }

    }

    fun tokenExpired(errCode: Int): Boolean {
        return errCode == SERVER_STATE_UNAUTHORIZED || errCode == 6007 || errCode == 6006
    }


    class ResponseThrowable(throwable: Throwable?, var code: Int) : Exception(throwable) {
        override var message: String = ""
    }


    class ServerError : RuntimeException() {
        var code = 0
        override var message: String = ""
    }

}