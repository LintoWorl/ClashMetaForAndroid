package app.hw.network

import app.hw.network.api.INetworkBaseInfo
import app.hw.network.interceptor.RequestInterceptor
import app.hw.network.interceptor.ResponseInterceptor
import app.hw.network.util.GsonHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @Time : created on 2024/4/22 20:19
 * @Description :网络模块的门户，负责Retrofit库的初始化和网络连接的通用配置
 */
object RetrofitManager {
    const val HTTP_TIMEOUT_CONNECT: Long = 10 * 1000L //网络请求连接超时时长
    private const val HTTP_TIMEOUT_READ: Long = 30 * 1000L
    private const val HTTP_TIMEOUT_WRITE: Long = 30 * 1000L
    internal lateinit var baseInfo: INetworkBaseInfo

    private val httpClient: OkHttpClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        OkHttpClient.Builder()
            .followSslRedirects(false)
            .retryOnConnectionFailure(true)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(HTTP_TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
            .readTimeout(HTTP_TIMEOUT_READ, TimeUnit.MILLISECONDS)
            .writeTimeout(HTTP_TIMEOUT_WRITE, TimeUnit.MILLISECONDS)
            .addInterceptor(RequestInterceptor())
            .addInterceptor(ResponseInterceptor())
            .build()
    }

    private val retrofit: Retrofit by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Retrofit.Builder().baseUrl(baseInfo.baseServerUrl()).client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonHelper.gson)).build()
    }

    /**
     * 网络库模块对外暴露的初始化方法
     */
    fun init(networkInfo: INetworkBaseInfo) {
        baseInfo = networkInfo
    }

    /**
     * 创建服务端接口服务
     */
    internal fun <T> createApiService(clazz: Class<T>): T = retrofit.create(clazz)

}