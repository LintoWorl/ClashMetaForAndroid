package app.hw.network.api

import android.util.Log
import app.hw.network.model.Constant
import app.hw.network.util.GsonHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.TreeMap

/**
 * @Time : created on 2024/4/22 18:12
 * @Description :构建网络请求参数
 */
class RequestParam(private val builder: Builder) {

    class Builder {

        val params: HashMap<String, Any> = HashMap()

        fun put(params: TreeMap<String, String>): Builder {
            params.putAll(params)
            return this
        }

        fun put(key: String?, value: String?): Builder {
            if (key == null || value == null) return this
            params[key] = value
            return this
        }

        fun put(key: String?, value: Number?): Builder {
            if (key == null || value == null) return this
            params[key] = value
            return this
        }

        fun put(key: String?, value: Array<String>?): Builder {
            if (key == null || value == null) return this
            params[key] = value
            return this
        }

        fun put(key: String?, value: List<String>?): Builder {
            if (key == null || value == null) return this
            params[key] = value
            return this
        }

        fun put(key: String?, value: Any?): Builder {
            if (key == null || value == null) return this
            params[key] = value
            return this
        }

        fun build(): RequestParam {
            //params["version"] = Utils.appVersion()
            val jsonData = GsonHelper.gson.toJson(params)
            Log.i(Constant.TAG_REQ, jsonData)
            return RequestParam(this)
        }

    }

    companion object {
        private val contentType = "application/json; charset=UTF-8".toMediaTypeOrNull()
        fun <T> getRequestBody(t: T): RequestBody {
            return t.toString().toRequestBody(contentType)
        }
    }

    val requestBody: RequestBody
        get() {
            return GsonHelper.gson.toJson(builder.params).toRequestBody(contentType)
        }

}