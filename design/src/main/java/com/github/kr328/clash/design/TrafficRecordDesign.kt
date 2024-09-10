package com.github.kr328.clash.design

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.hw.network.api.PaymentApi
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import app.hw.network.model.TrafficBean
import app.hw.network.util.NetworkUtil
import com.github.kr328.clash.design.adapter.TrafficRecordAdapter
import com.github.kr328.clash.design.databinding.DesignTrafficRecordBinding
import com.github.kr328.clash.design.util.applyFrom
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.root
import com.github.kr328.clash.design.util.show

class TrafficRecordDesign(context: Activity) : Design<Unit>(context) {
    private val binding =
        DesignTrafficRecordBinding.inflate(context.layoutInflater, context.root, false)
    private val recordAdapter = TrafficRecordAdapter(context)
    override val root: View
        get() = binding.root

    init {
        binding.activityBarLayout.applyFrom(context)
        binding.rvTrafficRecords.apply {
            adapter = recordAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            itemAnimator = null
            isNestedScrollingEnabled = false
        }
        val refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener { refresh ->
            RequestHandler.request({
                UserAccountApi.getTrafficLog()
            }, {
                refresh.finishRefresh()
                if (it.isEmpty() && recordAdapter.orderBeans.isEmpty()) {
                    emptyPage()
                } else {
                    updateList(it)
                }
            }, { code, msg ->
                refresh.finishRefresh()
                if (recordAdapter.orderBeans.isEmpty()) {
                    emptyPage()
                }
            })
        }
    }

    fun updateList(list: List<TrafficBean>) {
        recordAdapter.orderBeans = list
        recordAdapter.notifyItemRangeInserted(0, list.size)
    }

    fun emptyPage() {
        binding.refreshLayout.hide()
        binding.layoutEmpty.root.show()
        binding.layoutEmpty.tvEmptyDesc.text =
            if (NetworkUtil.isNetConnected(context)) "数据为空，请联系服务管理员" else "网络连接异常，请检查网络设置"
    }
}