package app.hw.network.api

import app.hw.network.RetrofitManager
import app.hw.network.model.OrderBean
import app.hw.network.model.PaymentBean
import app.hw.network.model.SubsProductBean

/**
 * @Time : created on 2024/4/23 13:51
 * @Description :应用内调用请求的充值和订阅商品相关的接口方法
 */
object PaymentApi {
    private val service: PaymentService by lazy { RetrofitManager.createService(PaymentService::class.java) }

    suspend fun getSubsPlan(guest: Boolean = false): ResponseData<List<SubsProductBean>> {
        return if (guest) service.getGuestSubsPlan() else service.getProductList()
    }

    suspend fun getOrderList(): ResponseData<List<OrderBean>> {
        return service.fetchOrder()
    }

    suspend fun getPayMethod(): ResponseData<List<PaymentBean>> {
        return service.getPayWay()
    }

    suspend fun getOrderInfo(tradeNo: String): ResponseData<OrderBean> {
        return service.getOrderDetail(tradeNo)
    }

    suspend fun getOrderStat(tradeNo: String): ResponseData<Int> {
        return service.checkOrderStat(tradeNo)
    }

    suspend fun createOrder(cycleName: String, planId: Int): ResponseData<String> {
        val reqBody = RequestParam.Builder().apply {
            put("cycle", cycleName)
            put("plan_id", planId)
        }.build().requestBody
        return service.saveOrder(reqBody)
    }

    suspend fun payOrder(tradeNo: String, method: Int): ResponseData<String> {
        val reqBody = RequestParam.Builder().apply {
            put("trade_no", tradeNo)
            put("method", method)
        }.build().requestBody
        return service.checkoutOrder(reqBody)
    }

    suspend fun cancelOrder(tradeNo: String): ResponseData<Boolean> {
        val reqBody = RequestParam.Builder().apply {
            put("trade_no", tradeNo)
        }.build().requestBody
        return service.cancelOrder(reqBody)
    }
}