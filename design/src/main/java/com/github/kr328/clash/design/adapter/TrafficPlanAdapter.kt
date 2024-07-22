package com.github.kr328.clash.design.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.hw.network.model.SubsProductBean
import com.github.kr328.clash.design.databinding.AdapterTrafficPlanBinding
import com.github.kr328.clash.design.util.layoutInflater

class TrafficPlanAdapter(val context: Context) : RecyclerView.Adapter<TrafficPlanAdapter.Holder>() {
    class Holder(val binding: AdapterTrafficPlanBinding) : RecyclerView.ViewHolder(binding.root)

    val planList = emptyList<SubsProductBean>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(AdapterTrafficPlanBinding.inflate(context.layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return planList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val plan = planList[position]
        holder.binding.tvPlanTitle.text = plan.name
        holder.binding.tvPlanPrice.text = plan.month_price.toString()
        holder.binding.tvPlanPeriod.text = "月付"
        holder.binding.tvPlanIntro.text = plan.sort
        holder.binding.tvPlanDesc.text = plan.content
    }
}