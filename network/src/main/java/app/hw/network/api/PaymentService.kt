package app.hw.network.api


/**
 * @Time : created on 2024/4/22 10:27
 * @Description :充值和订阅商品相关的服务端接口
 */
internal interface PaymentService {

    suspend fun getProduct()
}