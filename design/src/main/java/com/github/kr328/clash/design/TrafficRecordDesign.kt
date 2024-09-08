package com.github.kr328.clash.design

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.hw.network.model.TrafficBean
import com.github.kr328.clash.design.adapter.TrafficRecordAdapter
import com.github.kr328.clash.design.databinding.DesignTrafficRecordBinding
import com.github.kr328.clash.design.util.applyFrom
import com.github.kr328.clash.design.util.root

class TrafficRecordDesign(context: Activity) : Design<Unit>(context) {
    private val binding =
        DesignTrafficRecordBinding.inflate(context.layoutInflater, context.root, false)
    private val recordAdapter = TrafficRecordAdapter(context)
    override val root: View
        get() = binding.root

    init {
        binding.activityBarLayout.applyFrom(context)
        binding.rvTrafficRecords.apply {
            adapter = recordAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            itemAnimator = null
            isNestedScrollingEnabled = false
        }
    }

    fun updateList(list: List<TrafficBean>) {
        recordAdapter.orderBeans = list
        recordAdapter.notifyDataSetChanged()
    }

}