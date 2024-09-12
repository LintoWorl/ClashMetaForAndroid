package com.github.kr328.clash.design

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.hw.network.api.PaymentApi
import app.hw.network.handler.RequestHandler
import app.hw.network.model.CouponBean
import app.hw.network.model.OrderBean
import app.hw.network.model.SubsProductBean
import app.hw.network.util.NetworkUtil
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.common.util.TimeFormat
import com.github.kr328.clash.design.adapter.PayMethodAdapter
import com.github.kr328.clash.design.adapter.SubscribeOrderAdapter
import com.github.kr328.clash.design.databinding.DesignOrderManageBinding
import com.github.kr328.clash.design.databinding.DialogOrderConfirmBinding
import com.github.kr328.clash.design.databinding.DialogOrderFinishBinding
import com.github.kr328.clash.design.dialog.AppBottomSheetDialog
import com.github.kr328.clash.design.util.applyFrom
import com.github.kr328.clash.design.util.formatPrice
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.design.util.root
import com.github.kr328.clash.design.util.show

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
        val refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener { refresh ->
            RequestHandler.request({
                PaymentApi.getOrderList()
            }, {
                refresh.finishRefresh()
                if (it.isEmpty() && orderAdapter.orderBeans.isEmpty()) {
                    emptyPage()
                } else {
                    updateList(it)
                }
            }, { code, msg ->
                refresh.finishRefresh()
                if (orderAdapter.orderBeans.isEmpty()) {
                    emptyPage()
                }
            })
        }
        //refreshLayout.autoRefresh()
    }

    fun updateList(list: List<OrderBean>) {
        binding.refreshLayout.show()
        binding.layoutEmpty.root.hide()
        orderAdapter.orderBeans = list
        orderAdapter.notifyItemRangeInserted(0, list.size)
        //orderAdapter.notifyDataSetChanged()
    }

    fun emptyPage() {
        binding.refreshLayout.hide()
        binding.layoutEmpty.root.show()
        binding.layoutEmpty.tvEmptyDesc.text =
            if (NetworkUtil.isNetConnected(context)) "数据为空，请联系服务管理员" else "网络连接异常，请检查网络设置"
    }

    private fun checkDetail(orderBean: OrderBean) {
        when (orderBean.status) {
            0 -> {
                payOrder(orderBean)
            }

            2, 3 -> {
                showOrderDetail(orderBean)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun payOrder(order: OrderBean) {
        val dialog = AppBottomSheetDialog(context)

        val plan = order.plan
        val binding = DialogOrderConfirmBinding
            .inflate(context.layoutInflater, dialog.window?.decorView as ViewGroup?, false)
        binding.tvOrderDesc.text = plan?.name
        binding.tvPlanTraffic.text = "${plan?.transfer_enable}GB"
        binding.tvOrderNo.text = order.trade_no
        binding.tvOrderTime.text = TimeFormat.millis2String(order.created_at * 1000L)
        binding.tvOrderMoney.text = (order.total_amount / 100f).formatPrice()
        binding.tvOrderCoupon.text = (order.discount_amount / 100f).formatPrice()
        binding.tvCouponLabel.text = "优惠金额："
        plan?.apply {
            //getPlanPrice(this, binding.tvOrderPrice)
            setPlanPrice(this, binding.tvPlanPeriod, binding.tvOrderPrice)
        }

        val paymentAdapter = PayMethodAdapter(context) { _ -> }
        binding.tvOrderPay.onClickNew {
            if (paymentAdapter.payMethodList.isEmpty()) {
                Global.application.toast("请先设置支付方式")
                return@onClickNew
            }
            //TO 提交订单进行支付
            RequestHandler.request({
                PaymentApi.payOrder(order.trade_no, paymentAdapter.theChosenPayMethod.id)
            }, {
                Logger.d("submit order:$it")
                if (it.isEmpty()) {
                    Global.application.toast("支付订单失败！请重试")
                    return@request
                }
                val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                context.startActivity(actionIntent)
            }, { code, msg -> Global.application.toast(msg) })
        }
        binding.tvOrderCancel.onClickNew {
            RequestHandler.request({ PaymentApi.cancelOrder(order.trade_no) },
                {
                    Global.application.toast(if (it) "取消成功" else "取消失败了")
                }, { code, msg -> Global.application.toast(msg) })
            dialog.dismiss()
        }

        RequestHandler.request({
            PaymentApi.getPayMethod()
        }, {
            paymentAdapter.payMethodList = it
            paymentAdapter.notifyItemRangeInserted(0, it.size)
            //paymentAdapter.notifyDataSetChanged()
        }, { code, msg ->
            Global.application.toast(msg)
        })

        binding.rvPayMethods.apply {
            adapter = paymentAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            isNestedScrollingEnabled = false
        }

        binding.root.let { dialog.setContentView(it) }
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
        plan?.apply {
            setPlanPrice(plan, tvPrice = binding.tvOrderPrice)
        }

        binding.root.let { dialog.setContentView(it) }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun setPlanPrice(
        subsPlan: SubsProductBean,
        tvPeriod: TextView? = null,
        tvPrice: TextView
    ) {
        var subsPrice = subsPlan.month_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                tvPeriod?.text = "1个月"
                tvPrice.text = it.formatPrice()
                return
            }
        }

        subsPrice = subsPlan.quarter_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                tvPeriod?.text = "1季度"
                tvPrice.text = it.formatPrice()
                return
            }
        }

        subsPrice = subsPlan.half_year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                tvPeriod?.text = "半年（6个月）"
                tvPrice.text = it.formatPrice()
                return
            }
        }

        subsPrice = subsPlan.year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                tvPeriod?.text = "1年"
                tvPrice.text = it.formatPrice()
                return
            }
        }

        subsPrice = subsPlan.two_year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                tvPeriod?.text = "2年"
                tvPrice.text = it.formatPrice()
                return
            }
        }

        subsPrice = subsPlan.three_year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                tvPeriod?.text = "3年"
                tvPrice.text = it.formatPrice()
                return
            }
        }
    }

}