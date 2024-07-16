package app.hw.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import app.hw.network.RetrofitManager.baseInfo
import app.hw.network.model.Constant.DNS_AGENT1
import app.hw.network.model.Constant.DNS_AGENT2
import app.hw.network.model.Constant.DNS_AGENT3
import app.hw.network.model.Constant.DNS_TYPE_V4
import app.hw.network.model.Constant.DNS_TYPE_V6
import app.hw.network.model.Constant.DNS_V6_AGENT1
import app.hw.network.model.Constant.DNS_V6_AGENT2
import app.hw.network.model.Constant.PROTOCOL_HTTPS
import app.hw.network.util.NetworkUtil
import app.hw.network.util.NetworkUtil.isIpv6Address
import app.hw.network.util.SecureSSLSocketFactory
import com.github.kr328.clash.common.log.Log.TAG_EXP
import com.github.kr328.clash.common.log.Log.TAG_HTTP
import org.json.JSONObject
import java.net.URL
import java.net.URLConnection
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

object UrlConnManager {

    fun dohParse(context: Context, dm: String): String {
        if (!NetworkUtil.isNetConnected(context)) {
            return ""
        }
        val connV61 = getUrlConn(DNS_V6_AGENT1, dm, DNS_TYPE_V6)
        val result61 = try {
            connV61.inputStream.use { it.bufferedReader().readText() }
        } catch (e: Exception) {
            ""
        }
        if (result61.isNotBlank()) {
            val ip = parseData(result61)
            if (ip.isNotBlank()) {
                return ip
            }
        }

        val connV62 = getUrlConn(DNS_V6_AGENT2, dm, DNS_TYPE_V6)
        val result62 = try {
            connV62.inputStream.use { it.bufferedReader().readText() }
        } catch (e: Exception) {
            ""
        }
        if (result62.isNotBlank()) {
            val ip = parseData(result62)
            if (ip.isNotBlank()) {
                return ip
            }
        }

        val conn = getUrlConn(DNS_AGENT1, dm)
        val result1 = try {
            conn.inputStream.use { it.bufferedReader().readText() }
        } catch (e: Exception) {
            ""
        }
        if (result1.isNotBlank()) {
            val ip = parseData(result1)
            if (ip.isNotBlank()) {
                return ip
            }
        }

        val conn2 = getUrlConn(DNS_AGENT2, dm)
        val result2 = try {
            conn2.inputStream.use { it.bufferedReader().readText() }
        } catch (e: Exception) {
            ""
        }
        if (result2.isNotBlank()) {
            val ip = parseData(result2)
            if (ip.isNotBlank()) {
                return ip
            }
        }

        val conn3 = getUrlConn(DNS_AGENT3, dm)
        val result3 = try {
            conn3.inputStream.use { it.bufferedReader().readText() }
        } catch (e: Exception) {
            ""
        }
        if (result3.isNotBlank()) {
            val ip = parseData(result3)
            if (ip.isNotBlank()) {
                return ip
            }
        }
        return ""
    }

    private fun getUrlConn(agent: String, dm: String, type: String = DNS_TYPE_V4): URLConnection {
        val dns1Url = URL("${PROTOCOL_HTTPS}${agent}?name=$dm&type=$type")
        val conn = dns1Url.openConnection()
        conn.connectTimeout = 5000
        conn.readTimeout = 3000
        conn.setRequestProperty("Connection", "close")
        conn.setRequestProperty("accept", "application/dns-json")
        conn.useCaches = false
        return conn
    }

    private fun parseData(response: String): String {
        try {
            Log.d(TAG_HTTP, "parseIp:$response")
            val json = JSONObject(response)
            if (json.has("Answer")) {
                val ans = json.optJSONArray("Answer")
                if (ans != null && ans.length() > 0) {
                    val firAns = ans.getJSONObject(0)
                    val ipStr = firAns.optString("data", "")
                    return if (isIpv6Address(ipStr) && !ipStr.startsWith("[")) "[$ipStr]" else ipStr
                }
            }
        } catch (_: Exception) {
        }

        return ""
    }

    fun getUrlContent(urlStr: String): String {
        Log.d(TAG_HTTP, "it's going to request:$urlStr")
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpsURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 5000
        conn.setRequestProperty("Connection", "close")
        conn.useCaches = false
        return try {
            conn.inputStream.use {
                it.bufferedReader().readText()
            }
        } catch (e: Exception) {
            Log.e(TAG_HTTP, "network request fail:${e.message}")
            ""
        }
    }

    fun getUrlContentV2(
        urlStr: String, addHeader: Boolean = true, injectSni: Boolean = true
    ): String {
        Log.d(TAG_HTTP, "it's going to request:$urlStr")
        val url = URL(baseInfo.baseServerUrl() + urlStr)
        Log.d(TAG_HTTP,"getUrlContentV2, url:${url}")
        val conn = url.openConnection() as HttpsURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 5000
        conn.setRequestProperty("Connection", "close")
        conn.useCaches = true
        if (addHeader) {
            conn.setRequestProperty("vercode", baseInfo.appVerCode())
            conn.setRequestProperty("Host", "www.huawei.com")
            val netCountryCode = NetworkUtil.networkCountryISO(baseInfo.getAppContext())
            val localeCountry = Locale.getDefault().country
            conn.setRequestProperty("loc", "${netCountryCode}_$localeCountry")
            if (injectSni) {
                initSSLSocketFactory()?.let {
                    conn.sslSocketFactory = SecureSSLSocketFactory(conn, it)
                }
                conn.hostnameVerifier = HostnameVerifier { _, _ -> true }
            }
            conn.useCaches = false
        }
        return try {
            conn.inputStream.use {
                it.bufferedReader().readText()
            }
        } catch (e: Exception) {
            Log.e(TAG_EXP, "network request fail:${e.message}")
            ""
        }

    }

    private fun initSSLSocketFactory(): SSLSocketFactory? {
        try {
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
            };
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, Array(1) { trustManager }, SecureRandom())
            return sslContext.socketFactory
        } catch (_: Exception) {

        }
        return null
    }
}