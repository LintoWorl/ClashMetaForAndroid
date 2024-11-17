package app.hw.network.api

import app.hw.network.model.CouponBean
import app.hw.network.model.OrderBean
import app.hw.network.model.PaymentBean
import app.hw.network.model.SubsProductBean
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * @Time : created on 2024/4/22 10:27
 * @Description :充值和订阅商品相关的服务端接口
 */
internal interface PaymentService {

    @GET("/api/v1/guest/plan/fetch")
    suspend fun getGuestSubsPlan(): ResponseData<List<SubsProductBean>>

    @GET("/api/v1/user/plan/fetch")
    suspend fun getProductList(): ResponseData<List<SubsProductBean>>

    @GET("/api/v1/user/order/fetch")
    suspend fun fetchOrder(): ResponseData<List<OrderBean>>

    @GET("/api/v1/user/order/getPaymentMethod")
    suspend fun getPayWay(): ResponseData<List<PaymentBean>>

    @GET("/api/v1/user/order/details")
    suspend fun getOrderDetail(@Query("trade_no") tradeNo: String): ResponseData<OrderBean>

    @GET("/api/v1/user/order/check")
    suspend fun checkOrderStat(@Query("trade_no") tradeNo: String): ResponseData<Int>

    @POST("/api/v1/user/coupon/check")
    suspend fun checkCoupon(@Body requestBody: RequestBody): ResponseData<CouponBean>

    @POST("/api/v1/user/order/save")
    suspend fun saveOrder(@Body requestBody: RequestBody): ResponseData<String>

    @POST("/api/v1/user/order/checkout")
    suspend fun checkoutOrder(@Body requestBody: RequestBody): ResponseData<String>

    @POST("/api/v1/user/order/cancel")
    suspend fun cancelOrder(@Body requestBody: RequestBody): ResponseData<Boolean>
}