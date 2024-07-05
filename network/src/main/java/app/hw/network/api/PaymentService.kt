package app.hw.network.api

import app.hw.network.model.SubsProductBean
import retrofit2.http.GET
import retrofit2.http.POST


/**
 * @Time : created on 2024/4/22 10:27
 * @Description :充值和订阅商品相关的服务端接口
 */
internal interface PaymentService {

    @GET("/user/plan/fetch")
    suspend fun getProductList(): ResponseData<List<SubsProductBean>>

    @GET("/user/order/fetch")
    suspend fun fetchOrder():ResponseData<String>

    @GET("/user/order/getPaymentMethod")
    suspend fun getPayWay():ResponseData<String>

    @GET("/user/order/details?trade_no=")
    suspend fun getOrderDetail():ResponseData<String>

    @GET("/user/order/check?trade_no=")
    suspend fun checkOrderStat():ResponseData<String>

    @POST("/user/order/save")
    suspend fun saveOrder():ResponseData<String>

    @POST("/user/order/checkout")
    suspend fun checkoutOrder():ResponseData<String>

    @POST("/user/order/cancel")
    suspend fun cancelOrder():ResponseData<String>
}