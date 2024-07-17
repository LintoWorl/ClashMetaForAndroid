package app.hw.network

import android.annotation.SuppressLint
import android.os.Environment
import app.hw.network.api.INetworkBaseInfo
import app.hw.network.interceptor.RequestInterceptor
import app.hw.network.interceptor.ResponseInterceptor
import app.hw.network.util.NoSSLv3SocketFactory
import com.github.kr328.clash.common.log.Log
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.conn.ssl.SSLConnectionSocketFactory
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext


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
        val sslContext = SSLContext.getInstance("TLSv1")
        sslContext.init(null, null, SecureRandom())
        val noSSLv3SocketFactory = NoSSLv3SocketFactory(sslContext.socketFactory)
//        val dns = DnsOverHttps.Builder()
//            .url("https://1.1.1.1/dns-query".toHttpUrl())
//            .build()
        OkHttpClient.Builder()
            .followSslRedirects(false)
            .retryOnConnectionFailure(true)
            //.sslSocketFactory(noSSLv3SocketFactory, trustManager)
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
            .addConverterFactory(GsonConverterFactory.create()).build()
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


    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
    private val gson = GsonBuilder().setLenient().create()

    private val connectionSpecs: ArrayList<ConnectionSpec> =
        arrayListOf(ConnectionSpec.COMPATIBLE_TLS)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectionSpecs(connectionSpecs)
        .addInterceptor(logging)
        .addInterceptor(RequestInterceptor())
        .addInterceptor(ResponseInterceptor())
        .hostnameVerifier { _, _ -> true }
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()
    private val retrofit2: Retrofit by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Retrofit.Builder()
            .baseUrl(baseInfo.baseServerUrl())
            //.baseUrl("https://eight.8jiasu.com")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    fun <S> createService(serviceClass: Class<S>): S {
        return retrofit2.create(serviceClass)
    }


    suspend fun requestData(reqUrl: String, context: CoroutineContext = Dispatchers.IO) {
        withContext(context) {
            val client = OkHttpClient()
            try {
                val request = Request.Builder()
                    .url(reqUrl)
                    .get()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    //.header("User-Agent", "ClashforWindows/0.19.23")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("Response data is:$response")
                    }
                }
            } catch (e: Exception) {
                Log.e("request fail: $e")
            }
        }
    }
}