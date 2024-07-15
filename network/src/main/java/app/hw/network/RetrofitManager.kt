package app.hw.network

import android.annotation.SuppressLint
import app.hw.network.api.INetworkBaseInfo
import app.hw.network.interceptor.RequestInterceptor
import app.hw.network.interceptor.ResponseInterceptor
import app.hw.network.util.GsonHelper
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * @Time : created on 2024/4/22 20:19
 * @Description :网络模块的门户，负责Retrofit库的初始化和网络连接的通用配置
 */
object RetrofitManager {
    private const val HTTP_TIMEOUT_CONNECT: Long = 10 * 1000L //网络请求连接超时时长
    private const val HTTP_TIMEOUT_READ: Long = 30 * 1000L
    private const val HTTP_TIMEOUT_WRITE: Long = 30 * 1000L
    internal lateinit var baseInfo: INetworkBaseInfo

    private val httpClient: OkHttpClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val trustManager = @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {

            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {

            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, Array(1) { trustManager }, SecureRandom())
//        val dns = DnsOverHttps.Builder()
//            .url("https://1.1.1.1/dns-query".toHttpUrl())
//            .build()
        OkHttpClient.Builder()
            .followSslRedirects(false)
            //.retryOnConnectionFailure(true)
            //.sslSocketFactory(sslContext.socketFactory, trustManager)
            //.hostnameVerifier { _, _ -> true }
            .connectTimeout(HTTP_TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
            .readTimeout(HTTP_TIMEOUT_READ, TimeUnit.MILLISECONDS)
            .writeTimeout(HTTP_TIMEOUT_WRITE, TimeUnit.MILLISECONDS)
            //.dns(dns)
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