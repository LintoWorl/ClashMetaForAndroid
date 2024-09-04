package com.github.kr328.clash.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.hw.network.api.PaymentApi
import app.hw.network.handler.RequestHandler
import app.hw.network.model.PaymentBean
import app.hw.network.model.SubsProductBean
import com.github.kr328.clash.MainV2Activity
import com.github.kr328.clash.OrderListActivity
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.common.util.TimeFormat
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.design.adapter.PayMethodAdapter
import com.github.kr328.clash.design.adapter.TrafficPlanAdapter
import com.github.kr328.clash.design.databinding.DialogOrderConfirmBinding
import com.github.kr328.clash.design.databinding.FragTrafficStoreBinding
import com.github.kr328.clash.design.dialog.AppBottomSheetDialog
import com.github.kr328.clash.design.dialog.showModalProgressBar
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
    //private var orderDialogBinding: DialogOrderConfirmBinding? = null
    private var chosenPayment: PaymentBean? = null
    private var payMethods = emptyList<PaymentBean>()
    //private var subsOrderId: String = ""

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
        viewModel.getPaymentMethod()
        val refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener {
            viewModel.fetchSubsPlan(!appStore.hasLoginApp) { it.finishRefresh() }
        }
        planAdapter = TrafficPlanAdapter(activity) { plan ->
            RequestHandler.request({
                PaymentApi.createOrder("month_price", plan.id)
            }, {
                viewModel.subsOrderId = it
                createSubsOrder(plan)
            }, { code, msg ->
                Global.application.toast(msg)
                startActivity(OrderListActivity::class.intent)
            })
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

        viewModel.paymentMethodList.observe(viewLifecycleOwner) {
            chosenPayment = it[0]
            payMethods = it
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

    @SuppressLint("SetTextI18n")
    private fun createSubsOrder(plan: SubsProductBean) {
        val dialog = AppBottomSheetDialog(activity)

        val binding = DialogOrderConfirmBinding
            .inflate(activity.layoutInflater, dialog.window?.decorView as ViewGroup?, false)
        binding.tvOrderDesc.text = plan.name
        binding.tvOrderNo.text = viewModel.subsOrderId
        binding.tvOrderTime.text = TimeFormat.millis2String(System.currentTimeMillis())
        val df = DecimalFormat("#.00")
        binding.tvOrderPrice.text = "¥ " + df.format(plan.month_price / 100f)
        binding.tvOrderPay.onClickNew {
            chosenPayment?.apply {
                viewModel.commitSubsOrder(this)
                dialog.dismiss()
            }
        }
        binding.tvOrderCancel.onClickNew {
            viewModel.cancelSubsOrder()
            dialog.dismiss()
        }

        val paymentAdapter = PayMethodAdapter(activity) { payment ->
            chosenPayment = payment// 需更新支付方式的选中状态 FIXME
        }
        paymentAdapter.payMethodList = payMethods
        binding.rvPayMethods.apply {
            adapter = paymentAdapter
            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            isNestedScrollingEnabled = false
        }

        binding.root.let { dialog.setContentView(it) }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = StoreFragment()
    }
}