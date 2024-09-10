package com.github.kr328.clash

import app.hw.network.api.PaymentApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.OrdersDesign
import com.github.kr328.clash.design.dialog.showModalProgressBar

class OrderListActivity : BaseActivity<OrdersDesign>() {
    override suspend fun main() {
        val design = OrdersDesign(this)
        setContentDesign(design)
        //请求订单列表信息
        showModalProgressBar {
            configure {
                isIndeterminate = true
                text = "加载数据..."
            }
            fetchOrderList(design) { onResult() }
        }
    }

    private suspend fun fetchOrderList(design: OrdersDesign, finished: () -> Unit) {
        RequestHandler.request({
            PaymentApi.getOrderList()
        }, {
            if (it.isEmpty()) {
                design.emptyPage()
            } else {
                design.updateList(it)
            }
            finished()
        }, { code, msg ->
            finished()
            design.emptyPage()
            toast(msg)
        })
    }
}