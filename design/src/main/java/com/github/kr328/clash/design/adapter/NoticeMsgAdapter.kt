package com.github.kr328.clash.design.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.hw.network.model.NoticeBean
import com.github.kr328.clash.design.databinding.AdapterAppNoticeBinding
import com.github.kr328.clash.design.util.layoutInflater
import io.noties.markwon.Markwon

class NoticeMsgAdapter(
    private val context: Context,
) : RecyclerView.Adapter<NoticeMsgAdapter.Holder>() {
    class Holder(val binding: AdapterAppNoticeBinding) : RecyclerView.ViewHolder(binding.root)

    var noticeBeans = emptyList<NoticeBean>()
    private val markwon = Markwon.create(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(AdapterAppNoticeBinding.inflate(context.layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return noticeBeans.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val notice = noticeBeans[position]
        markwon.setMarkdown(holder.binding.tvNoticeTitle, notice.title)
        markwon.setMarkdown(holder.binding.tvNoticeContent, notice.content)
    }

}