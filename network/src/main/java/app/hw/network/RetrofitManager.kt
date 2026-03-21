package app.hw.network

import app.hw.network.api.INetworkBaseInfo
import app.hw.network.contant.Constant.DM_DIRECT
import app.hw.network.contant.Constant.DN_SECOND
import app.hw.network.contant.Constant.DN_THIRD
import app.hw.network.contant.Constant.PROTOCOL_HTTPS
import app.hw.network.interceptor.RequestInterceptor
import app.hw.network.interceptor.ResponseInterceptor
import app.hw.network.util.DnsUtil
import app.hw.network.util.UnsafeOkHttpClient
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.util.decode
import com.github.kr328.clash.common.util.encode
import com.github.kr328.clash.common.util.parseInetAddress
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.xbill.DNS.config.AndroidResolverConfigProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * @Time : created on 2024/4/22 20:19
 * @Description :网络模块的门户，负责Retrofit库的初始化和网络连接的通用配置
 */
object RetrofitManager {
    private const val HTTP_TIMEOUT_CONNECT: Long = 30 * 1000L //网络请求连接超时时长
    private const val HTTP_TIMEOUT_READ: Long = 30 * 1000L
    private const val HTTP_TIMEOUT_WRITE: Long = 30 * 1000L
    internal lateinit var baseInfo: INetworkBaseInfo
    private var strIp: String = ""
    private var dnFirst: String = ""
    private var dnSecond: String = ""
    private var dnThird: String = ""

    /**
     * 网络库模块对外暴露的初始化方法
     */
    fun init(networkInfo: INetworkBaseInfo) {
        baseInfo = networkInfo
        dnFirst = parseInetAddress(DM_DIRECT)
        dnSecond = parseInetAddress(DN_SECOND)
        dnThird = parseInetAddress(DN_THIRD)
        Logger.d("init network.")
        CoroutineScope(Dispatchers.IO).launch {
            Logger.d("init network, request the Ip of:$dnSecond")
            strIp = decode(networkInfo.preNetAdr())
            Logger.d("init network, get the preCachedIp:$strIp")
            val dnsUtil = DnsUtil()
            AndroidResolverConfigProvider.setContext(networkInfo.getAppContext())
            strIp = dnsUtil.lookupTxtRcd(dnThird)
            ////strIp = dnsUtil.getIpByHost(networkInfo.getAppContext(), dnSecond)
            Logger.d("init network, receive the Ip: $strIp")
            val encodedIp = encode(strIp)
            Logger.d("init network, encode the Ip:$encodedIp")
            networkInfo.updateNetAdr(encodedIp)
            //val firstIp = dnsUtil.getIpByHost(networkInfo.getAppContext(), dnFirst)
            //Logger.d("init network, receive the Ip of first DN: $firstIp")
        }
    }


    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val gson = GsonBuilder().setLenient().create()

    private val connectionSpecs: ArrayList<ConnectionSpec> =
        arrayListOf(ConnectionSpec.COMPATIBLE_TLS)
    private val myDns: Dns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            Logger.d("lookup hostname:$hostname, the parsedIp is:$strIp")
            if (strIp.isEmpty()) {
                AndroidResolverConfigProvider.setContext(Global.application)
                strIp = DnsUtil().lookupTxtRcd(dnThird)
                //strIp = DnsUtil().getIpByHost(Global.application, dnSecond)
                Logger.d("lookup got the hostname's ip:$strIp")
            }
            val ipList: List<InetAddress>
            if (strIp.isNotEmpty()) {
                ipList = ArrayList()
                ipList.add(InetAddress.getByName(strIp))
            } else {
                ipList = Dns.SYSTEM.lookup(dnSecond)
            }
            return ipList
        }
    }
    private val client: OkHttpClient = UnsafeOkHttpClient.getBuilder()
        //.proxy(Proxy.NO_PROXY)
        .connectionSpecs(connectionSpecs)
        .dns(myDns)
        .addInterceptor(logging)
        .addInterceptor(RequestInterceptor())
        .addInterceptor(ResponseInterceptor())
        .cache(null)
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

    private val okHttpClient: OkHttpClient = UnsafeOkHttpClient.getBuilder()
        //.proxy(Proxy.NO_PROXY)
        .connectionSpecs(connectionSpecs)
        //.dns(myDns)
        .addInterceptor(logging)
        .addInterceptor(RequestInterceptor())
        .addInterceptor(ResponseInterceptor())
        .cache(null)
        .connectTimeout(HTTP_TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
        .readTimeout(HTTP_TIMEOUT_READ, TimeUnit.MILLISECONDS)
        .writeTimeout(HTTP_TIMEOUT_WRITE, TimeUnit.MILLISECONDS)
        .build()
    private val retrofit: Retrofit by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Retrofit.Builder()
            .baseUrl("${PROTOCOL_HTTPS}${dnFirst}")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * 创建服务端接口服务
     */
    fun <S> createService(serviceClass: Class<S>): S {
        return retrofit2.create(serviceClass)
    }

    fun <T> createApiService(clazz: Class<T>): T {
        return retrofit.create(clazz)
    }
}