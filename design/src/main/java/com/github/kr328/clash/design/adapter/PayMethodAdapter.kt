package com.github.kr328.clash.design.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.hw.network.model.PaymentBean
import com.github.kr328.clash.design.databinding.AdapterPayMethodBinding
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.onClickNew

class PayMethodAdapter(val context: Context, val chosePayment: (PaymentBean) -> Unit) :
    RecyclerView.Adapter<PayMethodAdapter.Holder>() {
    class Holder(val binding: AdapterPayMethodBinding) : RecyclerView.ViewHolder(binding.root)

    var payMethodList = emptyList<PaymentBean>()
    private var lastCheckedPos = 0
    val theChosenPayMethod: PaymentBean
        get() {
            return payMethodList[lastCheckedPos]
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(AdapterPayMethodBinding.inflate(context.layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return payMethodList.size
    }

    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        val payMethod = payMethodList[position]
        holder.binding.tvPayMethod.text = payMethod.name
        holder.binding.ivPaymentChoiceStatus.isChecked = lastCheckedPos == position
        holder.binding.root.onClickNew {
            chosePayment(payMethod)
            holder.binding.ivPaymentChoiceStatus.isChecked = true
            if (lastCheckedPos != position) {
                notifyItemChanged(lastCheckedPos)
            }
            lastCheckedPos = position
        }
    }
}