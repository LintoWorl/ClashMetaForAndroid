package com.github.kr328.clash.design.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.hw.network.model.SubsProductBean
import com.github.kr328.clash.design.databinding.AdapterTrafficPlanBinding
import com.github.kr328.clash.design.util.formatPrice
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.design.util.show

class TrafficPlanAdapter(val context: Context, val subsPlan: (SubsProductBean, String) -> Unit) :
    RecyclerView.Adapter<TrafficPlanAdapter.Holder>() {
    class Holder(val binding: AdapterTrafficPlanBinding) : RecyclerView.ViewHolder(binding.root)

    var planList = emptyList<SubsProductBean>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(AdapterTrafficPlanBinding.inflate(context.layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return planList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val plan = planList[position]
        val payPeriod = getPlanPrice(plan, holder.binding)
        holder.binding.tvPlanTitle.text = plan.name
        holder.binding.tvPlanIntro.text = "${plan.transfer_enable}GB 流量"
        if (plan.content.isNullOrEmpty()) {
            holder.binding.btnPlanMore.hide()
            holder.binding.tvPlanDesc.hide()
        } else {
            holder.binding.btnPlanMore.show()
            val contentTxt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(plan.content, 0).toString()
            } else {
                Html.fromHtml(plan.content).toString()
            }
            holder.binding.tvPlanDesc.text = contentTxt
            holder.binding.btnPlanMore.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    holder.binding.tvPlanDesc.show()
                    holder.binding.divider.show()
                    holder.binding.btnPlanMore.text = "收起"
                } else {
                    holder.binding.tvPlanDesc.hide()
                    holder.binding.divider.hide()
                    holder.binding.btnPlanMore.text = "详情"
                }
            }
        }
        holder.binding.root.onClickNew { subsPlan(plan, payPeriod) }
        holder.binding.btnPlanBuy.onClickNew { subsPlan(plan, payPeriod) }
    }

    private fun getPlanPrice(
        subsPlan: SubsProductBean,
        binding: AdapterTrafficPlanBinding
    ): String {
        var subsPrice = subsPlan.month_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                setPriceText(it, "月付", binding)
                return "month_price"
            }
        }

        subsPrice = subsPlan.quarter_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                setPriceText(it, "季付", binding)
                return "quarter_price"
            }
        }

        subsPrice = subsPlan.half_year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                setPriceText(it, "半年付", binding)
                return "half_year_price"
            }
        }

        subsPrice = subsPlan.year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                setPriceText(it, "年付", binding)
                return "year_price"
            }
        }

        subsPrice = subsPlan.two_year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                setPriceText(it, "两年付", binding)
                return "two_year_price"
            }
        }

        subsPrice = subsPlan.three_year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                setPriceText(it, "三年付", binding)
                return "three_year_price"
            }
        }
        subsPrice = subsPlan.onetime_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                setPriceText(it, "", binding)
                return "onetime_price"
            }
        }
        return "month_price"
    }

    @SuppressLint("SetTextI18n")
    private fun setPriceText(price: Float, payDesc: String, binding: AdapterTrafficPlanBinding) {
        //val df = DecimalFormat("#.00")
        binding.tvPlanPrice.text = price.formatPrice()
        //"¥ " + if (price < 1.0f) "0${df.format(price)}元" else "${df.format(price)}元"
        binding.tvPlanPeriod.text = payDesc
    }
}