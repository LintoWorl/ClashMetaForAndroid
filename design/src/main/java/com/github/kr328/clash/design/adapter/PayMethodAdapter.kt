package com.github.kr328.clash.design.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.hw.network.model.PaymentBean
import com.github.kr328.clash.design.databinding.AdapterPayMethodBinding
import com.github.kr328.clash.design.util.layoutInflater

class PayMethodAdapter(val context: Context, val chosePayment: (PaymentBean) -> Unit) :
    RecyclerView.Adapter<PayMethodAdapter.Holder>() {
    class Holder(val binding: AdapterPayMethodBinding) : RecyclerView.ViewHolder(binding.root)

    var payMethodList = emptyList<PaymentBean>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(AdapterPayMethodBinding.inflate(context.layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return payMethodList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val payMethod = payMethodList[position]
        holder.binding.tvPayMethod.text = payMethod.name

    }
}