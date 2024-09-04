package com.github.kr328.clash.design

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.hw.network.model.OrderBean
import app.hw.network.model.SubsProductBean
import com.github.kr328.clash.common.util.TimeFormat
import com.github.kr328.clash.design.adapter.PayMethodAdapter
import com.github.kr328.clash.design.adapter.SubscribeOrderAdapter
import com.github.kr328.clash.design.databinding.DesignOrderManageBinding
import com.github.kr328.clash.design.databinding.DialogOrderConfirmBinding
import com.github.kr328.clash.design.databinding.DialogOrderFinishBinding
import com.github.kr328.clash.design.dialog.AppBottomSheetDialog
import com.github.kr328.clash.design.util.applyFrom
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.design.util.root
import java.text.DecimalFormat

class OrdersDesign(context: Activity) : Design<Unit>(context) {
    private val binding =
        DesignOrderManageBinding.inflate(context.layoutInflater, context.root, false)
    private val orderAdapter = SubscribeOrderAdapter(context, this::checkDetail)
    override val root: View
        get() = binding.root

    init {
        binding.activityBarLayout.applyFrom(context)
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
        if (orderBean.status == 0) {
            payOrder(orderBean)
        } else if (orderBean.status == 2) {
            showOrderDetail(orderBean)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun payOrder(order: OrderBean) {
        val dialog = AppBottomSheetDialog(context)

        val plan = order.plan
        val binding = DialogOrderConfirmBinding
            .inflate(context.layoutInflater, dialog.window?.decorView as ViewGroup?, false)
        binding.tvOrderDesc.text = plan?.name
        binding.tvOrderNo.text = order.trade_no
        binding.tvOrderTime.text = TimeFormat.millis2String(order.created_at * 1000L)
        val df = DecimalFormat("#.00")
        binding.tvOrderPrice.text = "¥ " + df.format(plan?.month_price?.div(100f) ?: 0)
        binding.tvOrderPay.onClickNew {
            //TODO 提交订单进行支付
        }
        binding.tvOrderCancel.onClickNew {
            //viewModel.cancelSubsOrder()//取消订单 TODO
            dialog.dismiss()
        }

        val paymentAdapter = PayMethodAdapter(context) { payment ->
            //chosenPayment = payment// 需更新支付方式的选中状态 FIXME
        }
        //paymentAdapter.payMethodList = payMethods //FIXME 添加支付方式
        binding.rvPayMethods.apply {
            adapter = paymentAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            isNestedScrollingEnabled = false
        }

        binding.root.let { dialog.setContentView(it) }
        //dialog.setCancelable(false)
        //dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showOrderDetail(orderBean: OrderBean) {
        val dialog = AppBottomSheetDialog(context)

        val plan = orderBean.plan
        val binding = DialogOrderFinishBinding
            .inflate(context.layoutInflater, dialog.window?.decorView as ViewGroup?, false)
        if (orderBean.status == 2) {
            binding.tvOrderStatus.text = "订单已取消"
            binding.tvLabelFinishDesc.text = "订单超时或已被主动取消"
            binding.ivStatusIcon.setImageResource(R.drawable.icon_cancel_tips)
        } else if (orderBean.status == 3) {
            binding.tvOrderStatus.text = "订单已完成"
            binding.tvLabelFinishDesc.text = "订单已完成支付"
            binding.ivStatusIcon.setImageResource(R.drawable.icon_finish_succ)
        }
        binding.tvOrderDesc.text = plan?.name
        binding.tvOrderNo.text = orderBean.trade_no
        binding.tvOrderTime.text = TimeFormat.millis2String(orderBean.created_at * 1000L)
        val df = DecimalFormat("#.00")
        binding.tvOrderPrice.text = "¥ " + df.format(plan?.month_price?.div(100f) ?: 0)

        binding.root.let { dialog.setContentView(it) }
        dialog.show()
    }
}