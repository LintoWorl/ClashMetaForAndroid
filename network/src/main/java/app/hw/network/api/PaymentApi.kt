package app.hw.network.api

import app.hw.network.RetrofitManager
import app.hw.network.model.CouponBean
import app.hw.network.model.OrderBean
import app.hw.network.model.PaymentBean
import app.hw.network.model.SubsProductBean
import app.hw.network.util.NetworkUtil
import com.github.kr328.clash.common.Global

/**
 * @Time : created on 2024/4/23 13:51
 * @Description :应用内调用请求的充值和订阅商品相关的接口方法
 */
object PaymentApi {
    private val service: PaymentService by lazy { RetrofitManager.createService(PaymentService::class.java) }
    private val service2: PaymentService by lazy { RetrofitManager.createApiService(PaymentService::class.java) }

    suspend fun getSubsPlan(guest: Boolean = false): ResponseData<List<SubsProductBean>> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return if (guest) service2.getGuestSubsPlan() else service2.getProductList()
        }
        return if (guest) service.getGuestSubsPlan() else service.getProductList()
    }

    suspend fun getOrderList(): ResponseData<List<OrderBean>> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.fetchOrder()
        }
        return service.fetchOrder()
    }

    suspend fun getPayMethod(): ResponseData<List<PaymentBean>> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.getPayWay()
        }
        return service.getPayWay()
    }

    suspend fun getOrderInfo(tradeNo: String): ResponseData<OrderBean> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.getOrderDetail(tradeNo)
        }
        return service.getOrderDetail(tradeNo)
    }

    suspend fun getOrderStat(tradeNo: String): ResponseData<Int> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.checkOrderStat(tradeNo)
        }
        return service.checkOrderStat(tradeNo)
    }

    suspend fun checkCoupon(couponCode: String, planId: Int): ResponseData<CouponBean> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.checkCoupon(couponCode, planId)
        }
        return service.checkCoupon(couponCode, planId)
    }

    suspend fun createOrder(cycleName: String, planId: Int, couponCode: String?): ResponseData<String> {
        val reqBody = RequestParam.Builder().apply {
            put("period", cycleName)
            put("plan_id", planId)
            put("coupon_code", couponCode)
        }.build().requestBody
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.saveOrder(reqBody)
        }
        return service.saveOrder(reqBody)
    }

    suspend fun payOrder(tradeNo: String, method: Int): ResponseData<String> {
        val reqBody = RequestParam.Builder().apply {
            put("trade_no", tradeNo)
            put("method", method)
        }.build().requestBody
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.checkoutOrder(reqBody)
        }
        return service.checkoutOrder(reqBody)
    }

    suspend fun cancelOrder(tradeNo: String): ResponseData<Boolean> {
        val reqBody = RequestParam.Builder().apply {
            put("trade_no", tradeNo)
        }.build().requestBody
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.cancelOrder(reqBody)
        }
        return service.cancelOrder(reqBody)
    }
}