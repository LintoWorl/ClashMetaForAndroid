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

    @GET("guest/plan/fetch")
    suspend fun getGuestSubsPlan(): ResponseData<List<SubsProductBean>>

    @GET("user/plan/fetch")
    suspend fun getProductList(): ResponseData<List<SubsProductBean>>

    @GET("user/order/fetch")
    suspend fun fetchOrder(): ResponseData<List<OrderBean>>

    @GET("user/order/getPaymentMethod")
    suspend fun getPayWay(): ResponseData<List<PaymentBean>>

    @GET("user/order/details")
    suspend fun getOrderDetail(@Query("trade_no") tradeNo: String): ResponseData<OrderBean>

    @GET("user/order/check")
    suspend fun checkOrderStat(@Query("trade_no") tradeNo: String): ResponseData<Int>

    @GET("user/coupon/check")
    suspend fun checkCoupon(
        @Query("code") code: String,
        @Query("plan_id") planId: Int
    ): ResponseData<CouponBean>

    @POST("user/order/save")
    suspend fun saveOrder(@Body requestBody: RequestBody): ResponseData<String>

    @POST("user/order/checkout")
    suspend fun checkoutOrder(@Body requestBody: RequestBody): ResponseData<String>

    @POST("user/order/cancel")
    suspend fun cancelOrder(@Body requestBody: RequestBody): ResponseData<Boolean>
}