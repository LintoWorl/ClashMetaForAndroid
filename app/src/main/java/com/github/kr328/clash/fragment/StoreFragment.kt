package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kr328.clash.MainV2Activity
import com.github.kr328.clash.design.adapter.TrafficPlanAdapter
import com.github.kr328.clash.design.databinding.FragTrafficStoreBinding
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class StoreFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var binding: FragTrafficStoreBinding
    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var activity: MainV2Activity
    private lateinit var planAdapter: TrafficPlanAdapter
    private lateinit var appStore: AppStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = requireActivity() as MainV2Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragTrafficStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        appStore = AppStore(activity)
        viewModel.fetchSubsPlan(!appStore.hasLoginApp)
        initObserver()
    }

    private fun initView() {
        val refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener {
            viewModel.fetchSubsPlan(!appStore.hasLoginApp) { it.finishRefresh() }
        }
        planAdapter = TrafficPlanAdapter(activity)
        binding.rvTrafficPlan.apply {
            adapter = planAdapter
            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            isNestedScrollingEnabled = false
        }
    }

    private fun initObserver() {
        viewModel.subsPlanList.observe(viewLifecycleOwner) {
            planAdapter.planList = it
            planAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = StoreFragment()
    }
}