package app.hw.network.api

/**
 * @Time : created on 2024/4/22 16:45
 * @Description :
 */
class ResponseData<T> {
    var code: Int = 200
    var message: String? = null
    var data: T? = null
}