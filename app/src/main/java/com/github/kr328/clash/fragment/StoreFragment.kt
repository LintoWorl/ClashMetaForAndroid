package com.github.kr328.clash.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.hw.network.api.PaymentApi
import app.hw.network.handler.RequestHandler
import app.hw.network.model.CouponBean
import app.hw.network.model.PaymentBean
import app.hw.network.model.SubsProductBean
import app.hw.network.util.NetworkUtil
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
import com.github.kr328.clash.design.dialog.EditableDialog
import com.github.kr328.clash.design.dialog.showModalProgressBar
import com.github.kr328.clash.design.util.formatPrice
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.design.util.show
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

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
    //private var chosenPlan: SubsProductBean? = null
    //private var chosenPlanPayCycle: String = ""

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
        planAdapter = TrafficPlanAdapter(activity, this::subscribeThePlan)
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
            if (planAdapter.planList.isEmpty() && it.isNullOrEmpty()) {
                binding.refreshLayout.hide()
                binding.layoutEmpty.root.show()
                binding.layoutEmpty.tvEmptyDesc.text =
                    if (NetworkUtil.isNetConnected(activity)) "数据为空，请联系服务管理员" else "网络连接异常，请检查网络设置"
                return@observe
            }
            if (it.isNotEmpty()) {
                binding.refreshLayout.show()
                binding.layoutEmpty.root.hide()
                planAdapter.planList = it
                planAdapter.notifyDataSetChanged()
            }
        }
        viewModel.lgnState.observe(viewLifecycleOwner) {
            requireRefresh = true
        }

        viewModel.paymentMethodList.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) return@observe
            chosenPayment = it[0]
            payMethods = it
        }

        binding.layoutEmpty.root.onClickNew { fetchData() }
    }

    private fun fetchData() {
        launch {
            activity.showModalProgressBar {
                configure {
                    isIndeterminate = true
                    text = "加载套餐数据..."
                }
                viewModel.fetchSubsPlan(!appStore.hasLoginApp) { onResult() }
            }
        }
        requireRefresh = false
    }

    private fun subscribeThePlan(plan: SubsProductBean, period: String) {
        //检测用户身份，游客用户需先登录
        if (!viewModel.userHasLogin) {
            viewModel.prevFragIdx = MainViewModel.IDX_FRAG_SUBS
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
            return
        }
        if (payMethods.isEmpty()) {
            viewModel.getPaymentMethod()
        }

        //弹框编辑优惠券
        EditableDialog.show(activity.supportFragmentManager) {
            title = "优惠券"
            leftButton = "不用了"
            rightButton = "去验证"
            listener = object : EditableDialog.OnClickListener {
                override fun onPositiveClick(dialog: EditableDialog, editContent: String) {
                    if (editContent.isEmpty()) {
                        activity.toast("输入的优惠券码不能为空～")
                        return
                    }
                    viewModel.validateCoupon(editContent, plan) {
                        dialog.dismiss()
                        questOrder(plan, period, it)
                    }
                }

                override fun onNegativeClick(dialog: EditableDialog) {
                    dialog.dismiss()
                    questOrder(plan, period, null)
                }
            }
        }
    }

    private fun questOrder(plan: SubsProductBean, period: String, coupon: CouponBean?) {
        RequestHandler.request({
            PaymentApi.createOrder(period, plan.id, coupon?.code)
        }, {
            viewModel.subsOrderId = it
            createSubsOrder(plan, coupon)
        }, { code, msg ->
            Global.application.toast(msg)
            startActivity(OrderListActivity::class.intent)
        })
    }

    @SuppressLint("SetTextI18n")
    private fun createSubsOrder(plan: SubsProductBean, coupon: CouponBean?) {
        val dialog = AppBottomSheetDialog(activity)

        val binding = DialogOrderConfirmBinding
            .inflate(activity.layoutInflater, dialog.window?.decorView as ViewGroup?, false)
        binding.tvOrderNo.text = viewModel.subsOrderId
        binding.tvOrderTime.text = TimeFormat.millis2String(System.currentTimeMillis())
        //binding.tvOrderPrice.text = "¥ " + df.format(plan.month_price / 100f)
        setPlanPrice(plan, coupon, binding)
        binding.tvOrderDesc.text = plan.name
        binding.tvPlanTraffic.text = "${plan.transfer_enable}GB"
        binding.tvOrderPay.onClickNew {
            chosenPayment?.apply {
                dialog.dismiss()
                viewModel.commitSubsOrder(this)
            }
        }
        binding.tvOrderCancel.onClickNew {
            dialog.dismiss()
            viewModel.cancelSubsOrder()
        }

        val paymentAdapter = PayMethodAdapter(activity) { payment ->
            chosenPayment = payment// 需更新支付方式的选中状态
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

    @SuppressLint("SetTextI18n")
    private fun setPlanPrice(
        subsPlan: SubsProductBean,
        coupon: CouponBean?,
        binding: DialogOrderConfirmBinding
    ) {
        val tvPeriod = binding.tvPlanPeriod
        var subsPrice = subsPlan.month_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                tvPeriod.text = "1个月"
                calcOrderPrice(it, coupon, binding)
                return
            }
        }

        subsPrice = subsPlan.quarter_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                calcOrderPrice(it, coupon, binding)
                tvPeriod.text = "1季度"
                return
            }
        }

        subsPrice = subsPlan.half_year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                calcOrderPrice(it, coupon, binding)
                tvPeriod.text = "半年（6个月）"
                return
            }
        }

        subsPrice = subsPlan.year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                calcOrderPrice(it, coupon, binding)
                tvPeriod.text = "1年"
                return
            }
        }

        subsPrice = subsPlan.two_year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                calcOrderPrice(it, coupon, binding)
                tvPeriod.text = "2年"
                return
            }
        }

        subsPrice = subsPlan.three_year_price?.let { it / 100f }
        subsPrice?.let {
            if (it > 0f) {
                calcOrderPrice(it, coupon, binding)
                tvPeriod.text = "3年"
                return
            }
        }
    }

    private fun calcOrderPrice(it: Float, coupon: CouponBean?, binding: DialogOrderConfirmBinding) {
        val tvPrice = binding.tvOrderPrice
        val tvOffer = binding.tvOrderMoney
        val tvCoupon = binding.tvOrderCoupon
        //val tvPeriod = binding.tvPlanPeriod
        tvPrice.text = it.formatPrice()
        if (coupon == null) {
            tvCoupon.text = "无优惠"
            tvOffer.text = it.formatPrice()
        } else {
            tvCoupon.text = coupon.name
            tvOffer.text = when (coupon.type) {
                1 -> (it - coupon.value / 100f).formatPrice()
                2 -> (it * (1.0f - coupon.value / 100f)).formatPrice()
                else -> it.formatPrice()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = StoreFragment()
    }
}