package com.github.kr328.clash.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.util.NetworkUtil
import com.github.kr328.clash.AppSettingsActivity
import com.github.kr328.clash.BaseActivity
import com.github.kr328.clash.BuildConfig
import com.github.kr328.clash.LogsActivity
import com.github.kr328.clash.MainV2Activity
import com.github.kr328.clash.MetaFeatureSettingsActivity
import com.github.kr328.clash.NetworkSettingsActivity
import com.github.kr328.clash.OrderListActivity
import com.github.kr328.clash.OverrideSettingsActivity
import com.github.kr328.clash.R
import com.github.kr328.clash.TrafficRecordActivity
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.common.util.encode
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.packageName
import com.github.kr328.clash.core.bridge.Bridge
import com.github.kr328.clash.design.databinding.FragUserCenterBinding
import com.github.kr328.clash.design.util.DATE_DATE_ONLY
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.design.util.show
import com.github.kr328.clash.design.util.toDateStr
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class UserFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragUserCenterBinding

    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var activity: MainV2Activity
    private var reqrdRefresh: Boolean = false
    private lateinit var appStore: AppStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = requireActivity() as MainV2Activity
        appStore = AppStore(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragUserCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvents()
        initObserver()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && reqrdRefresh) {
            initView()
        }
        if (!hidden) {
            viewModel.fetchSubscribeInfo()
            viewModel.updateUserInfo()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        if (viewModel.userHasLogin) {
            launch { viewModel.fetchUserAccountInfo(activity) }

            binding.tvAccountEmail.isClickable = false
            binding.tvAccountEmail.isEnabled = false
            binding.llOrderTraffics.show()
            binding.btnLogout.show()
            binding.btnResetPwd.show()
            binding.llSubsInfo.show()
        } else {
            binding.tvAccountEmail.text = "尚未登录，马上登录 >>"
            binding.tvAccountEmail.isClickable = true
            binding.tvAccountEmail.isEnabled = true
            binding.llOrderTraffics.hide()
            binding.tvLastLogin.hide()
            binding.llSubsInfo.hide()
            binding.btnLogout.hide()
            binding.btnResetPwd.hide()
        }
        binding.tvAppVersion.text = requireActivity().packageManager.getPackageInfo(
            packageName, 0
        ).versionName + "\n" + Bridge.nativeCoreVersion()
        reqrdRefresh = false
        if (BuildConfig.DEBUG) {
            binding.itemAppLogs.show()
            binding.dividerAboveLogs.show()
        } else {
            binding.itemAppLogs.hide()
            binding.dividerAboveLogs.hide()
        }
        //binding.itemTgGroup.hide()
        //binding.dividerUnderTg.hide()
    }

    private fun initEvents() {
        binding.btnLogout.onClickNew {
            appStore.hasLoginApp = false
            appStore.enteredHome = false
            appStore.authData = ""
            viewModel.userHasLogin = false
            viewModel.lgnState.postValue(false)
            activity.events.trySend(BaseActivity.Event.ClashStop)
            activity.stopClashService()
            context?.toast("退出登录成功")
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
            //请求退出登录API
        }
        binding.itemOfficialWeb.onClickNew {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(appStore.appWebsite)
                )
            )
        }
        binding.itemAppTos.onClickNew {
            if (appStore.tosAddress.isEmpty()) return@onClickNew
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(appStore.tosAddress)
                )
            )
        }
        binding.itemAppFeedback.onClickNew {
            feedback()
        }
        binding.itemTgGroup.onClickNew {
            val actionIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.conn_us_tg)))
            startActivity(actionIntent)
        }
        binding.btnResetPwd.onClickNew {
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_XPWD
        }
        binding.tvAccountEmail.onClickNew {
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
        }

        binding.itemMyOrders.onClickNew {
            startActivity(OrderListActivity::class.intent)
        }
        binding.itemMyTraffic.onClickNew {
            startActivity(TrafficRecordActivity::class.intent)
        }

        binding.itemAppSetting.onClickNew {
            startActivity(AppSettingsActivity::class.intent)
        }
        binding.itemNetworkConfig.onClickNew {
            startActivity(NetworkSettingsActivity::class.intent)
        }
        binding.itemOverrideTraffic.onClickNew {
            startActivity(OverrideSettingsActivity::class.intent)
        }
        binding.itemAppFeatures.onClickNew {
            startActivity(MetaFeatureSettingsActivity::class.intent)
        }
        binding.itemAppLogs.onClickNew {
            startActivity(LogsActivity::class.intent)
        }
        binding.itemCheckVersion.onClickNew {
            //
            context?.toast("已是最新版本")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        viewModel.subsInfo.observe(viewLifecycleOwner) {
            if (!viewModel.userHasLogin || it.plan == null || it.expired_at <= 0) {
                binding.llSubsInfo.hide()
                return@observe
            }
            val subsPlan = it?.plan ?: return@observe
            binding.llSubsInfo.show()
            binding.tvSubsTitle.text = subsPlan.name
        }
        viewModel.userInfo.observe(viewLifecycleOwner) {
            if (!viewModel.userHasLogin) return@observe
            binding.tvAccountEmail.text = it.email
            if (it.expired_at > 0) {
                binding.tvSubsDesc.text =
                    "套餐到期时间：${it.expired_at.toDateStr(DATE_DATE_ONLY)}"
            } else {
                binding.llSubsInfo.hide()
            }
            //val lastLgnTime = if (it.last_login_at > 0) it.last_login_at else it.created_at
            if (it.last_login_at > 0) {
                binding.tvLastLogin.show()
                binding.tvLastLogin.text =
                    "账号注册时间：${it.last_login_at.toDateStr(DATE_DATE_ONLY)}"
            } else {
                binding.tvLastLogin.hide()
            }
        }
        viewModel.lgnState.observe(viewLifecycleOwner) {
            reqrdRefresh = true
        }
    }

    private fun feedback() {
        val dataIntent = Intent(Intent.ACTION_SENDTO)
        dataIntent.data = Uri.parse("mailto:joinmibox@pm.me")
        dataIntent.putExtra(Intent.EXTRA_SUBJECT, "反馈问题及建议")
        val version = "App版本: ${BuildConfig.VERSION_NAME}"
        val model = "手机型号: ${Build.MANUFACTURER}-${Build.MODEL}"
        val plat = "系统版本: ${Build.VERSION.RELEASE}"
        val uIp = "问题代码: ${encode(NetworkUtil.getIpAddress(requireContext()))}"

        val stringBuilder = StringBuilder()
            .append(version).append("\n")
            .append(model).append("\n")
            .append(plat).append("\n")
            .append(uIp).append("\n")
        dataIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
        startActivity(dataIntent)
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserFragment()
    }
}