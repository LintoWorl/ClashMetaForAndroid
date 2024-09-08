package com.github.kr328.clash

import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.design.TrafficRecordDesign

class TrafficRecordActivity : BaseActivity<TrafficRecordDesign>() {
    override suspend fun main() {
        val design = TrafficRecordDesign(this)
        setContentDesign(design)
        //请求流量明细列表信息
        fetchTrafficRecordList(design)
    }

    private suspend fun fetchTrafficRecordList(design: TrafficRecordDesign) {
        RequestHandler.request({
            UserAccountApi.getTrafficLog()
        }, {
            design.updateList(it)
        }, { code, msg -> })
    }
}