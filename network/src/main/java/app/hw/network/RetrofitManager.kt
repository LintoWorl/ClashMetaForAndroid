package app.hw.network

import app.hw.network.api.INetworkBaseInfo
import app.hw.network.interceptor.MyDns
import app.hw.network.interceptor.RequestInterceptor
import app.hw.network.util.UnsafeOkHttpClient
import com.google.gson.GsonBuilder
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @Time : created on 2024/4/22 20:19
 * @Description :网络模块的门户，负责Retrofit库的初始化和网络连接的通用配置
 */
object RetrofitManager {
    private const val HTTP_TIMEOUT_CONNECT: Long = 10 * 1000L //网络请求连接超时时长
    private const val HTTP_TIMEOUT_READ: Long = 30 * 1000L
    private const val HTTP_TIMEOUT_WRITE: Long = 30 * 1000L
    internal lateinit var baseInfo: INetworkBaseInfo

    /**
     * 网络库模块对外暴露的初始化方法
     */
    fun init(networkInfo: INetworkBaseInfo) {
        baseInfo = networkInfo
    }


    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val gson = GsonBuilder().setLenient().create()

    private val connectionSpecs: ArrayList<ConnectionSpec> =
        arrayListOf(ConnectionSpec.COMPATIBLE_TLS)

    private val client: OkHttpClient = UnsafeOkHttpClient.getBuilder()
        .connectionSpecs(connectionSpecs)
        .dns(MyDns())
        .addInterceptor(logging)
        .addInterceptor(RequestInterceptor())
        //.addInterceptor(ResponseInterceptor())
        .connectTimeout(HTTP_TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
        .readTimeout(HTTP_TIMEOUT_READ, TimeUnit.MILLISECONDS)
        .writeTimeout(HTTP_TIMEOUT_WRITE, TimeUnit.MILLISECONDS)
        .build()
    private val retrofit2: Retrofit by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Retrofit.Builder()
            .baseUrl(baseInfo.baseServerUrl())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    /**
     * 创建服务端接口服务
     */
    fun <S> createService(serviceClass: Class<S>): S {
        return retrofit2.create(serviceClass)
    }

}