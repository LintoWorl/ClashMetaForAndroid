package app.hw.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException

/**
 * @Time : created on 2024/4/22 11:20
 * @Description :接口请求的响应拦截器
 */
class ResponseInterceptor : Interceptor {
    private val TAG = "Http-Res"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        //FIXME
        printRes(response)
        return response
    }

    private fun printRes(response: Response): Response {
        try {
            val builder = response.newBuilder()
            val clone = builder.build()
            var body = clone.body
            if (body != null) {
                val mediaType = body.contentType()
                if (mediaType != null) {
                    if (isText(mediaType)) {
                        val resp = body.string()
                        Log.i(TAG, "//======================start========================\\\\")
                        Log.i(TAG, "url=" + clone.request.url)
                        Log.i(TAG, resp)
                        Log.i(TAG, "\\\\======================end==========================//")
                        body = ResponseBody.create(mediaType, resp)
                        return response.newBuilder().body(body).build()
                    } else {
                        Log.i(TAG, "data:" + " maybe[file part] , too large too print , ignored!")
                    }
                } else {
                    Log.i(TAG, "contentType is null")
                }
            } else {
                Log.i(TAG, "body is null")
            }
        } catch (e: Exception) {
            e.localizedMessage?.let { Log.i(TAG, it) }
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