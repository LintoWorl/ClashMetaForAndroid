package app.hw.network.interceptor

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import app.hw.network.RetrofitManager.baseInfo
import app.hw.network.util.NetworkUtil
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * @Time : created on 2024/4/22 11:19
 * @Description :添加请求头信息的拦截器
 */
class RequestInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.addHeader("Content-Type", "application/json;charset=utf-8")
        //builder.addHeader("deviceId", PhoneSystemUtils.getDeviceId())//FIXME
        builder.addHeader("clientIp", NetworkUtil.getIpAddress(baseInfo.getAppContext()))
        builder.addHeader("vername", baseInfo.appVerName())
        builder.addHeader("vercode", baseInfo.appVerCode())
        builder.addHeader("manufacture", Build.MANUFACTURER)
        builder.addHeader("deviceModel", Build.MODEL)
        builder.addHeader("osVer", Build.VERSION.RELEASE)
        builder.addHeader("clientTime", Date().time.toString())
        builder.addHeader("timezone", TimeZone.getDefault().id)
        builder.addHeader("language", Locale.getDefault().language)
        val netCountryCode = getNetWorkCountryISO(baseInfo.getAppContext())
        val localeCountry = Locale.getDefault().country
        builder.addHeader("loc", "${netCountryCode}_$localeCountry")
        builder.addHeader("appType", "8007")//区分app
        builder.addHeader("Host", "www.huawei.com")
        return chain.proceed(builder.build())
    }

    private fun getNetWorkCountryISO(context: Context): String {
        val telManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (telManager.networkCountryIso == null) "US" else telManager.networkCountryIso
    }
}