package app.hw.network.api

import app.hw.network.RetrofitManager
import app.hw.network.model.ProductBean

/**
 * @Time : created on 2024/4/23 13:51
 * @Description :应用内调用请求的充值和订阅商品相关的接口方法
 */
object PaymentApi {
    private val service: PaymentService by lazy { RetrofitManager.createApiService(PaymentService::class.java) }

    suspend fun getCoinProducts(): ResponseData<ArrayList<ProductBean>> {
        return service.getCoinProducts()
    }

    suspend fun getVipProducts(): ResponseData<ArrayList<ProductBean>> {
        return service.getVipProducts()
    }

    suspend fun createOncePayOrder(): ResponseData<String> {
        return service.createOncePayOrder()
    }

    suspend fun deliverOncePayCoin(): ResponseData<String> {
        return service.deliverOncePayCoin()
    }

    suspend fun verifyOncePayOrder(): ResponseData<String> {
        return service.verifyOncePayOrder()
    }

    suspend fun createVipOrder(): ResponseData<String> {
        return service.createVipOrder()
    }

    suspend fun deliverVipRights(): ResponseData<String> {
        return service.deliverVipRights()
    }

    suspend fun restoreVipRights(): ResponseData<String> {
        return service.restoreVipRights()
    }

    suspend fun acceptVipRewards(): ResponseData<String> {
        return service.acceptVipRewards()
    }

    suspend fun queryVipRights(): ResponseData<String> {
        return service.queryVipRights()
    }

    suspend fun queryCoinRights(): ResponseData<String> {
        return service.queryCoinRights()
    }
}