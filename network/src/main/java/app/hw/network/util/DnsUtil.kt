package app.hw.network.util

import android.content.Context
import android.util.Log
import app.hw.network.contant.Constant
import com.github.kr328.clash.common.log.Logger.TAG_HTTP
import org.json.JSONObject
import java.net.URL
import java.net.URLConnection

class DnsUtil {
    fun getIpByHost(context: Context, dm: String): String {
        if (!NetworkUtil.isNetConnected(context)) {
            return ""
        }
        val connV61 = getUrlConn(Constant.DNS_V6_AGENT1, dm, Constant.DNS_TYPE_V6)
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

        val connV62 = getUrlConn(Constant.DNS_V6_AGENT2, dm, Constant.DNS_TYPE_V6)
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

        val conn = getUrlConn(Constant.DNS_AGENT1, dm)
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

        val conn2 = getUrlConn(Constant.DNS_AGENT2, dm)
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

        val conn3 = getUrlConn(Constant.DNS_AGENT3, dm)
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

    private fun getUrlConn(
        agent: String,
        dm: String,
        type: String = Constant.DNS_TYPE_V4
    ): URLConnection {
        val dns1Url = URL("${Constant.PROTOCOL_HTTPS}${agent}?name=$dm&type=$type")
        val conn = dns1Url.openConnection()
        conn.connectTimeout = 5000
        conn.readTimeout = 3000
        conn.setRequestProperty("Connection", "close")
        conn.setRequestProperty("accept", "application/dns-json")
        conn.useCaches = true
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
                    return if (NetworkUtil.isIpv6Address(ipStr) && !ipStr.startsWith("[")) "[$ipStr]" else ipStr
                }
            }
        } catch (_: Exception) {
        }

        return ""
    }
}