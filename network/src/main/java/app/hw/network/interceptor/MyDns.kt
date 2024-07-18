package app.hw.network.interceptor

import android.util.Log
import app.hw.network.util.DnsUtil
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Log.TAG_HTTP
import okhttp3.Dns
import okhttp3.Dns.Companion.SYSTEM
import java.net.InetAddress

class MyDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        Log.d(TAG_HTTP, "lookup hostname:$hostname")
        val strIp = DnsUtil().getIpByHost(Global.application, "eight.8jiasu.com")
        Log.d(TAG_HTTP, "got the hostname's ip:$strIp")
        val ipList: List<InetAddress>
        if (strIp.isNotEmpty()) {
            ipList = ArrayList()
            ipList.add(InetAddress.getByName(strIp))
        } else {
            ipList = SYSTEM.lookup(hostname)
        }
        return ipList
    }
}