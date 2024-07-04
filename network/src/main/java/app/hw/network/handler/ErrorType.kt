package app.hw.network.handler

/**
 * @Time : created on 2024/4/22 13:50
 * @Description :定义接口请求中的错误类型和错误码
 */
object ErrorType {
    //未授权
    const val SERVER_STATE_UNAUTHORIZED = 401

    //禁止的
    const val SERVER_STATE_FORBIDDEN = 403

    //未找到
    const val SERVER_STATE_UNFOUND = 404

    //请求超时
    const val SERVER_STATE_REQUEST_TIMEOUT = 408

    //内部服务器错误
    const val SERVER_STATE_INTERNAL_ERROR = 500

    //错误网关
    const val SERVER_STATE_BAD_GATEWAY = 502

    //暂停服务
    const val SERVER_STATE_UNAVAILABLE = 503

    //网关超时
    const val SERVER_STATE_GATEWAY_TIMEOUT = 504


    /**
     * 未知错误
     */
    const val ERROR_UNKNOWN = 1000

    /**
     * 解析失败
     */
    const val ERROR_PARSE_FAIL = 1001

    /**
     * 网络连接失败
     */
    const val ERROR_NETWORK_FAIL = 1002

    /**
     * 协议出错
     */
    const val ERROR_HTTP_PROTOCOL = 1003

    /**
     * 证书出错
     */
    const val ERROR_SSL_EXCEPTION = 1005

    /**
     * 连接超时
     */
    const val ERROR_CONN_TIMEOUT = 1006

}