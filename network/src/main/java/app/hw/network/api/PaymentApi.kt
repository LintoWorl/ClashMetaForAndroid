package app.hw.network.api

import app.hw.network.RetrofitManager
import app.hw.network.model.SubsProductBean

/**
 * @Time : created on 2024/4/23 13:51
 * @Description :应用内调用请求的充值和订阅商品相关的接口方法
 */
object PaymentApi {
    private val service: PaymentService by lazy { RetrofitManager.createApiService(PaymentService::class.java) }

    suspend fun getSubsPlan(): ResponseData<List<SubsProductBean>> {
        return service.getProductList()
    }
}