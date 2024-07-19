package app.hw.network.interceptor

import android.util.Log
import app.hw.network.BuildConfig
import com.github.kr328.clash.common.log.Log.TAG_HTTP
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import java.io.IOException

/**
 * @Time : created on 2024/4/22 11:20
 * @Description :接口请求的响应拦截器
 */
class ResponseInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (BuildConfig.DEBUG) {
            printRes(response)
        }
        return response
    }

    private fun printRes(response: Response): Response {
        try {
            val body = response.peekBody(Long.MAX_VALUE).string()
            if (body.isNotEmpty()) {
                Log.i(TAG_HTTP, "/======================start========================\\")
                Log.i(TAG_HTTP, "url=" + response.request.url)
                Log.i(TAG_HTTP, body)
                Log.i(TAG_HTTP, "\\=======================end=========================/")
            } else {
                Log.i(TAG_HTTP, "data:" + " maybe[file part] , too large too print , ignored!")
            }
        } catch (e: Exception) {
            e.localizedMessage?.let { Log.i(TAG_HTTP, it) }
        }
        return response
    }

    /**
     * is text data?
     */
    private fun isText(mediaType: MediaType?): Boolean {
        return if (mediaType == null) false else {
            "text" == mediaType.subtype || "json" == mediaType.subtype || "xml" == mediaType.subtype
                    || "html" == mediaType.subtype || "webviewhtml" == mediaType.subtype || "x-www-form-urlencoded" == mediaType.subtype
        }
    }
}