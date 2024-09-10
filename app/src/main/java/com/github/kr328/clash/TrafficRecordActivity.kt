package com.github.kr328.clash

import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.TrafficRecordDesign
import com.github.kr328.clash.design.dialog.showModalProgressBar
import kotlinx.coroutines.launch

class TrafficRecordActivity : BaseActivity<TrafficRecordDesign>() {
    override suspend fun main() {
        val design = TrafficRecordDesign(this)
        setContentDesign(design)
        //请求流量明细列表信息
        launch {
            showModalProgressBar {
                configure {
                    isIndeterminate = true
                    text = "加载数据..."
                }
                fetchTrafficRecordList(design) { onResult() }
            }
        }
    }

    private suspend fun fetchTrafficRecordList(design: TrafficRecordDesign, finished: () -> Unit) {
        RequestHandler.request({
            UserAccountApi.getTrafficLog()
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