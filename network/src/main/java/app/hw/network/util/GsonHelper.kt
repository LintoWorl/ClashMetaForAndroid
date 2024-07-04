package app.hw.network.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * @Time : created on 2024/4/22 18:14
 * @Description :网络请求Gson解析器
 */
object GsonHelper {
    val gson: Gson by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        GsonBuilder().setLenient().create()
    }

    fun <T> parseBean(json: String, clazz: Class<T>): T? {
        try {
            return gson.fromJson(json, clazz)
        } catch (e: Error) {
            Log.e("ParseBean", e.toString())
        }
        return null
    }

}