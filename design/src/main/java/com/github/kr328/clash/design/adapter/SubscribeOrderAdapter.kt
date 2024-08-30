package com.github.kr328.clash.design.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.hw.network.model.OrderBean
import app.hw.network.model.OrderStatus
import com.github.kr328.clash.common.util.TimeFormat
import com.github.kr328.clash.design.databinding.AdapterSubscribeOrderBinding
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.onClickNew
import java.text.DecimalFormat

/**
 * 我的订单管理界面的列表
 */
class SubscribeOrderAdapter(val context: Context, val chosePayment: (OrderBean) -> Unit) :
    RecyclerView.Adapter<SubscribeOrderAdapter.Holder>() {
    class Holder(val binding: AdapterSubscribeOrderBinding) : RecyclerView.ViewHolder(binding.root)

    var orderBeans = emptyList<OrderBean>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(AdapterSubscribeOrderBinding.inflate(context.layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return orderBeans.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val payMethod = orderBeans[position]
        holder.binding.tvSubsOrderTitle.text =
            payMethod.plan?.name?.takeIf { it.isNotEmpty() } ?: "默认套餐"
        val df = DecimalFormat("#.00")
        holder.binding.tvSubsOrderPrice.text = "¥ " + df.format(payMethod.total_amount / 100f)
        holder.binding.tvSubsOrderTime.text = TimeFormat.millis2String(payMethod.created_at * 1000L)
        holder.binding.tvSubsOrderState.text = OrderStatus().getStatusDesc(payMethod.status)
        holder.binding.root.onClickNew {
            chosePayment(payMethod)
        }
    }
}