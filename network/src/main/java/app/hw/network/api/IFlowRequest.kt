package app.hw.network.api

/**
 * @Time : created on 2024/4/23 15:54
 * @Description :流式请求相关的配置和回调方法
 */
interface IFlowRequest {
    /**
     * 流式请求的完整地址
     */
    fun requestUrl(): String

    /**
     * 请求数据的签名Key值
     */
    fun signKey(): String

    /**
     * 请求的超时时间，默认未3秒
     */
    fun timeout(): Int = 3
    fun error(msg: String)
    fun finish(code: Int, content: String)
}