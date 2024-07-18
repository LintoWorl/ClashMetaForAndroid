package com.github.kr328.clash.design.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.util.layoutInflater

class MailAddressAdapter(
    private val context: Context,
    private val addressList: List<String>
) : BaseAdapter() {
    override fun getCount(): Int {
        return addressList.size
    }

    override fun getItem(position: Int): Any {
        return addressList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: context.layoutInflater.inflate(
            R.layout.adapter_mail_address, parent, false
        )

        val text: TextView = view.findViewById(R.id.tv_mail_address)
        val current = addressList[position]
        text.text = "@$current"
        return view
    }
}