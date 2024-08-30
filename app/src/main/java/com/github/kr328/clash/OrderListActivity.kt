package com.github.kr328.clash

import app.hw.network.api.PaymentApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.design.OrdersDesign

class OrderListActivity : BaseActivity<OrdersDesign>() {
    override suspend fun main() {
        val design = OrdersDesign(this)
        setContentDesign(design)
        //请求订单列表信息
        fetchOrderList(design)
    }

    private suspend fun fetchOrderList(design: OrdersDesign) {
        RequestHandler.request({
            PaymentApi.getOrderList()
        }, {
            design.updateList(it)
        }, { code, msg -> })
    }
}