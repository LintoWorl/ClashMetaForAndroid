package app.hw.network.api

import android.app.Application

/**
 * @Time : created on 2024/4/23 11:26
 * @Description :网路库依赖的一些基础信息，比如应用Context、服务接口地址、应用版本信息等
 */
interface INetworkBaseInfo {
    /**
     * 获取应用上下文参数
     */
    fun getAppContext(): Application

    /**
     * 获取服务端接口的域名地址
     */
    fun baseServerUrl(): String

    /**
     * 应用的版本号
     */
    fun appVerCode(): String

    /**
     * 应用的版本名称
     */
    fun appVerName(): String
}