package app.hw.network.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URL
import java.util.Collections


object NetworkUtil {

    private var pingUrl = "http://www.google.com"

    /**
     * NetworkAvailable
     */
    private var PING_SUCCESS = 1

    /**
     * no NetworkAvailable
     */
    private var PING_TIMEOUT = 2

    /**
     * Net no ready
     */
    private var NET_NOT_PREPARE = 3

    /**
     * net error
     */
    private var NET_ERROR = 4

    /**
     * TIMEOUT
     */
    private const val TIMEOUT = 3000


    /**
     * check Network Available
     *
     * @param context
     * @return
     */
    fun isNetworkValid(context: Context): Boolean {
        try {
            val manager = context.applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
            val info = manager.activeNetworkInfo
            return null != info && info.isAvailable
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * return network state
     *
     * @param context
     * @return
     */
    fun getNetState(context: Context): Int {
        try {
            val connectivity = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivity.activeNetworkInfo
            if (networkInfo != null) {
                return if (networkInfo.isAvailable && networkInfo.isConnected) {
                    if (!connectionNetwork()) {
                        PING_TIMEOUT
                    } else {
                        PING_SUCCESS
                    }
                } else {
                    NET_NOT_PREPARE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return NET_ERROR
    }

    /**
     * ping google
     *
     * @return
     */
    private fun connectionNetwork(): Boolean {
        var result = false
        var httpUrl: HttpURLConnection? = null
        try {
            httpUrl = URL(pingUrl).openConnection() as? HttpURLConnection
            httpUrl?.connectTimeout = TIMEOUT
            httpUrl?.connect()
            result = true
        } catch (e: IOException) {
            e.message?.let { Log.d("TAG", it) }
        } finally {
            httpUrl?.disconnect()
            httpUrl = null
        }
        return result
    }

    /**
     * 异步检测网址是否可以访问。
     *
     * @param url
     * @param timeout
     * @param listener
     */
    fun checkUrlCanConnectAsync(url: String, timeout: Int = 1000, listener: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = checkUrlCanConnect(url, timeout)
            CoroutineScope(Dispatchers.Main).launch {
                listener(result)
            }
        }
    }

    /**
     * 检测某一个网址是否可以连接
     * @return
     */
    fun checkUrlCanConnect(url: String, timeout: Int = 1000): Boolean {
        var result = false
        var httpUrl: HttpURLConnection? = null
        try {
            httpUrl = URL(url)
                .openConnection() as HttpURLConnection
            httpUrl.connectTimeout = timeout
            httpUrl.connect()

            result = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                httpUrl?.disconnect()
            } catch (ee: Exception) {
                ee.printStackTrace()
            }

        }
        return result
    }

    /**
     * isWifi
     *
     * @param context
     * @return boolean
     */
    fun isWifi(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return if (activeNetInfo != null
            && activeNetInfo.type == ConnectivityManager.TYPE_WIFI
        ) {
            true
        } else false
    }

    fun getNetworkType(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val networkType = when {
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WI-FI"
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "CELLULAR"
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "ETHERNET"
            else -> "UNKNOWN"
        }
        return networkType
    }

    fun getIpAddress(context: Context?): String {
        if (context == null) {
            return ""
        }
        val conMann = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= 29) {
            val network = conMann.activeNetwork
            val capabilities = conMann.getNetworkCapabilities(network)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return getLocalIpAddress() ?: ""
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return getWifiAddress(context)
                }
            }
        } else {
            val mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            val wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (mobileNetworkInfo != null && mobileNetworkInfo.isConnected) {
                return getLocalIpAddress() ?: ""
            } else if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected) {
                return getWifiAddress(context)
            }
        }
        return ""
    }

    private fun getLocalIpAddress(): String? {
        try {
            val nilist = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (ni in nilist) {
                val ialist = Collections.list(ni.inetAddresses)
                for (address in ialist) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.getHostAddress()
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getWifiAddress(context: Context?): String {
        if (context == null) {
            return ""
        }
        try {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            return intToIp(ipAddress)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun intToIp(ipInt: Int): String {
        return (ipInt and 0xFF).toString() + "." +
                (ipInt shr 8 and 0xFF) + "." +
                (ipInt shr 16 and 0xFF) + "." +
                (ipInt shr 24 and 0xFF)
    }

}