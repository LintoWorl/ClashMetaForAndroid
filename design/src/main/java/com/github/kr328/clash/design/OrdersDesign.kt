package com.github.kr328.clash.design

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.hw.network.model.OrderBean
import com.github.kr328.clash.design.adapter.SubscribeOrderAdapter
import com.github.kr328.clash.design.databinding.DesignOrderManageBinding
import com.github.kr328.clash.design.util.applyFrom
import com.github.kr328.clash.design.util.root

class OrdersDesign(context: Activity) : Design<Unit>(context) {
    private val binding =
        DesignOrderManageBinding.inflate(context.layoutInflater, context.root, false)
    private val orderAdapter = SubscribeOrderAdapter(context, this::checkDetail)
    override val root: View
        get() = binding.root

    init {
        binding.activityBarLayout.applyFrom(context)
        //binding.titleBar.titleBarText.text = "我的订单"
        //binding.titleBar.titleBarGoback.onClickNew { context.finish() }
        binding.rvSubsPlanOrder.apply {
            adapter = orderAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            itemAnimator = null
            isNestedScrollingEnabled = false
        }
    }

    fun updateList(list: List<OrderBean>) {
        orderAdapter.orderBeans = list
        orderAdapter.notifyDataSetChanged()
    }

    private fun checkDetail(orderBean: OrderBean) {

    }
}