package com.github.kr328.clash.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.hw.network.model.PaymentBean
import com.github.kr328.clash.MainV2Activity
import com.github.kr328.clash.design.adapter.PayMethodAdapter
import com.github.kr328.clash.design.adapter.TrafficPlanAdapter
import com.github.kr328.clash.design.databinding.DialogOrderConfirmBinding
import com.github.kr328.clash.design.databinding.DialogProfilesMenuBinding
import com.github.kr328.clash.design.databinding.FragTrafficStoreBinding
import com.github.kr328.clash.design.dialog.AppBottomSheetDialog
import com.github.kr328.clash.design.dialog.showModalProgressBar
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class StoreFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var binding: FragTrafficStoreBinding
    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var activity: MainV2Activity
    private lateinit var planAdapter: TrafficPlanAdapter
    private lateinit var appStore: AppStore
    private var requireRefresh: Boolean = false
    private var orderDialogBinding: DialogOrderConfirmBinding? = null
    private var chosenPayment: PaymentBean? = null

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
        appStore = AppStore(activity)
        initView()
        initObserver()
        fetchData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && requireRefresh) {
            fetchData()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        val refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener {
            viewModel.fetchSubsPlan(!appStore.hasLoginApp) { it.finishRefresh() }
        }
        planAdapter = TrafficPlanAdapter(activity) { plan ->
            //创建flow，等待创建订单和获取支付方式两个接口
            viewModel.getPaymentMethod()
            viewModel.createSubsPlanOrder(plan)
            val dialog = AppBottomSheetDialog(activity)

            orderDialogBinding = DialogOrderConfirmBinding
                .inflate(activity.layoutInflater, dialog.window?.decorView as ViewGroup?, false)
            orderDialogBinding?.tvOrderDesc?.text = plan.name
            val df = DecimalFormat("#.00")
            orderDialogBinding?.tvOrderPrice?.text = "¥ " + df.format(plan.month_price / 100f)
            orderDialogBinding?.tvOrderPay?.onClickNew {
                chosenPayment?.apply {
                    viewModel.commitSubsOrder(this)
                }
            }
            orderDialogBinding?.tvOrderCancel?.onClickNew { viewModel.cancelSubsOrder() }

            orderDialogBinding?.root?.let { dialog.setContentView(it) }
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
        binding.rvTrafficPlan.apply {
            adapter = planAdapter
            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            isNestedScrollingEnabled = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        viewModel.subsPlanList.observe(viewLifecycleOwner) {
            planAdapter.planList = it
            planAdapter.notifyDataSetChanged()
        }
        viewModel.lgnState.observe(viewLifecycleOwner) {
            requireRefresh = true
        }
        viewModel.subsOrderId.observe(viewLifecycleOwner) {
            orderDialogBinding?.tvOrderNo?.text = it
        }
        viewModel.paymentMethodList.observe(viewLifecycleOwner) {
            val paymentAdapter = PayMethodAdapter(activity) {
                chosenPayment = it
            }
            paymentAdapter.payMethodList = it
            orderDialogBinding?.rvPayMethods?.apply {
                adapter = paymentAdapter
                layoutManager =
                    LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                itemAnimator = null
                isNestedScrollingEnabled = false
            }
        }
    }

    private fun fetchData() {
        launch {
            activity.showModalProgressBar {
                configure {
                    isIndeterminate = true
                    text = "加载数据..."
                }
                viewModel.fetchSubsPlan(!appStore.hasLoginApp) { onResult() }
            }
        }
        requireRefresh = false
    }

    companion object {
        @JvmStatic
        fun newInstance() = StoreFragment()
    }
}