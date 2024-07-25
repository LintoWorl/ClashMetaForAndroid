package com.github.kr328.clash.design.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.hw.network.model.SubsProductBean
import com.github.kr328.clash.design.databinding.AdapterTrafficPlanBinding
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.design.util.show
import java.text.DecimalFormat

class TrafficPlanAdapter(val context: Context) : RecyclerView.Adapter<TrafficPlanAdapter.Holder>() {
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
        holder.binding.tvPlanTitle.text = plan.name
        val df = DecimalFormat("#.00")
        holder.binding.tvPlanPrice.text = "¥ " + df.format(plan.month_price / 100f)
        holder.binding.tvPlanPeriod.text = "月付"
        holder.binding.tvPlanIntro.text = plan.sort ?: "88GB 流量"
        holder.binding.tvPlanDesc.text = plan.content
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
}