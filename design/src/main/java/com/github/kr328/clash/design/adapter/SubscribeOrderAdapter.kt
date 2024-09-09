package com.github.kr328.clash.design.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.hw.network.model.OrderBean
import app.hw.network.model.OrderStatus
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.util.TimeFormat
import com.github.kr328.clash.design.databinding.AdapterSubscribeOrderBinding
import com.github.kr328.clash.design.util.formatPrice
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.onClickNew

/**
 * 我的订单管理界面的列表
 */
class SubscribeOrderAdapter(val context: Context, val clickedItem: (OrderBean) -> Unit) :
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
        val orderBean = orderBeans[position]
        holder.binding.tvSubsOrderTitle.text =
            orderBean.plan?.name?.takeIf { it.isNotEmpty() } ?: "默认套餐"
        val payCycle = orderBean.period
        Logger.d("the period of subscribe is:$payCycle")
        orderBean.plan?.apply {
            when (payCycle) {
                "month_price" -> holder.binding.tvSubsOrderPrice.text =
                    month_price?.let { (it / 100f).formatPrice() }

                "quarter_price" -> holder.binding.tvSubsOrderPrice.text =
                    quarter_price?.let { (it / 100f).formatPrice() }

                "half_year_price" -> holder.binding.tvSubsOrderPrice.text =
                    half_year_price?.let { (it / 100f).formatPrice() }

                "year_price" -> holder.binding.tvSubsOrderPrice.text =
                    year_price?.let { (it / 100f).formatPrice() }

                "two_year_price" -> holder.binding.tvSubsOrderPrice.text =
                    two_year_price?.let { (it / 100f).formatPrice() }

                "three_year_price" -> holder.binding.tvSubsOrderPrice.text =
                    three_year_price?.let { (it / 100f).formatPrice() }
            }
        }

        holder.binding.tvSubsOrderTime.text = TimeFormat.millis2String(orderBean.created_at * 1000L)
        holder.binding.tvSubsOrderState.text = OrderStatus().getStatusDesc(orderBean.status)
        holder.binding.root.onClickNew {
            clickedItem(orderBean)
        }
    }
}