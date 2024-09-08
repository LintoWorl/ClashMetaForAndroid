package com.github.kr328.clash.design.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.hw.network.model.TrafficBean
import com.github.kr328.clash.common.util.TimeFormat
import com.github.kr328.clash.common.util.TimeFormat.FORMAT_YYYY_MM_DD
import com.github.kr328.clash.design.databinding.AdapterTrafficRecordBinding
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.toBytesString

/**
 * 我的订单管理界面的列表
 */
class TrafficRecordAdapter(val context: Context) :
    RecyclerView.Adapter<TrafficRecordAdapter.Holder>() {
    class Holder(val binding: AdapterTrafficRecordBinding) : RecyclerView.ViewHolder(binding.root)

    var orderBeans = emptyList<TrafficBean>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(AdapterTrafficRecordBinding.inflate(context.layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return orderBeans.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val orderBean = orderBeans[position]
        holder.binding.tvTrafficRecordTime.text =
            TimeFormat.millis2String(orderBean.record_at * 1000L, FORMAT_YYYY_MM_DD)
        holder.binding.tvTrafficUp.text = orderBean.u.toBytesString()
        holder.binding.tvTrafficDown.text = orderBean.d.toBytesString()
        holder.binding.tvTrafficTotal.text = (orderBean.u + orderBean.d).toBytesString()
    }
}