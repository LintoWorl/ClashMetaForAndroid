package app.hw.network.interceptor

import android.os.Build
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
        builder.addHeader("appVersion", baseInfo.appVerName())
        builder.addHeader("versionCode", baseInfo.appVerCode())
        builder.addHeader("manufacture", Build.MANUFACTURER)
        builder.addHeader("deviceModel", Build.MODEL)
        builder.addHeader("osVersion", Build.VERSION.RELEASE)
        builder.addHeader("clientTime", Date().time.toString())
        builder.addHeader("timezone", TimeZone.getDefault().id)
        builder.addHeader("language", Locale.getDefault().language)
        //builder.addHeader("isoCountryCode", Utils.getNetWorkCountryISO())
        //builder.addHeader("appId", Constants.bid)//FIXME
        builder.addHeader("appType", "8007")//区分app
        builder.addHeader("region", Locale.getDefault().country)
        //builder.addHeader("traceCode", Utils.createTraceCode())
        //builder.addHeader("setLanguage", MultiLanguageUtil.getCurrentLanguageCode())//FIXME
        return chain.proceed(builder.build())
    }

}