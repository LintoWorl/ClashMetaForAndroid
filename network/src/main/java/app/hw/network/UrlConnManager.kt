package app.hw.network

import android.annotation.SuppressLint
import android.util.Log
import app.hw.network.RetrofitManager.baseInfo
import app.hw.network.contant.Constant.DM_BACKUP
import app.hw.network.util.NetworkUtil
import app.hw.network.util.SecureSSLSocketFactory
import com.github.kr328.clash.common.log.Logger.TAG_EXP
import com.github.kr328.clash.common.log.Logger.TAG_HTTP
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

object UrlConnManager {

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
        Log.d(TAG_HTTP, "getUrlContentV2, url:${url}")
        val conn = url.openConnection() as HttpsURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 5000
        conn.setRequestProperty("Connection", "close")
        conn.useCaches = true
        if (addHeader) {
            conn.setRequestProperty("vercode", baseInfo.appVerCode())
            conn.setRequestProperty("Host", DM_BACKUP)
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