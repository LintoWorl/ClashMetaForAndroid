package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.model.UserInfo
import app.hw.network.util.GsonHelper
import app.hw.network.util.NetworkUtil
import com.github.kr328.clash.BaseActivity
import com.github.kr328.clash.MainV2Activity
import com.github.kr328.clash.ProxyActivity
import com.github.kr328.clash.R
import com.github.kr328.clash.common.datastore.DataRepository
import com.github.kr328.clash.design.R as designR
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.common.util.TimeFormat
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.ticker
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.design.HomeDesign
import com.github.kr328.clash.design.dialog.CommonDialog
import com.github.kr328.clash.design.dialog.ModelProgressBarConfigure
import com.github.kr328.clash.design.dialog.showModalProgressBar
import com.github.kr328.clash.design.ui.ToastDuration
import com.github.kr328.clash.design.util.toDateStr
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.store.TipsStore
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.util.withClash
import com.github.kr328.clash.util.withProfile
import com.github.kr328.clash.vm.MainViewModel
import com.github.kr328.clash.vm.ViewModelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var design: HomeDesign
    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var activity: MainV2Activity
    private var refreshSubsInfo: Boolean = false
    private var hasReqInitMsg: Boolean = false

    val clashRunning: Boolean
        get() = Remote.broadcasts.clashRunning

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = requireActivity() as MainV2Activity
        design = HomeDesign(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return design.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val runningVpn = NetworkUtil.isVpnRunning(activity)
        Logger.d("onViewCreated in HomeFrag, has running:$runningVpn, clashRunning=$clashRunning")
        if (viewModel.appConfig.value == null && !runningVpn) {
            CoroutineScope(Dispatchers.Main).launch {
                activity.showModalProgressBar {
                    configure {
                        isIndeterminate = true
                        text = "正在初始化网络环境，请稍等片刻..."
                    }
                    viewModel.appConfig.observe(viewLifecycleOwner) {
                        onResult()
                    }
                }
            }
        }
        initObserver()
        observeClashStat()
        if (!hasReqInitMsg) {
            viewModel.updateUserInfo()
            viewModel.fetchNoticeInfo()
            viewModel.fetchSubscribeInfo()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && refreshSubsInfo) {
            viewModel.fetchSubscribeInfo()
        }
        if (!hidden) {
            observeClashStat()
            launch { design.fetch() }
            viewModel.fetchNoticeInfo()
        }
    }

    private fun observeClashStat() {
        launch {
            val ticker = ticker(TimeUnit.SECONDS.toMillis(1))

            while (isActive) {
                select {
                    activity.events.onReceive {
                        when (it) {
                            BaseActivity.Event.ActivityStart, BaseActivity.Event.ServiceRecreated,
                            BaseActivity.Event.ClashStop, BaseActivity.Event.ClashStart,
                            BaseActivity.Event.ProfileLoaded, BaseActivity.Event.ProfileChanged -> {
                                Logger.i("In HomeFrag, receive event:${it.name}")
                                design.fetch()
                            }

                            else -> Unit
                        }
                    }
                    design.requests.onReceive {
                        when (it) {
                            HomeDesign.Request.ToggleStatus -> {
                                if (clashRunning) {
                                    activity.stopClashService()
                                    viewModel.guestConnClash = false
                                } else {
                                    if (!viewModel.userHasLogin) {
                                        startClash()
                                        viewModel.guestConnClash = true
                                    } else {
                                        if (viewModel.guestConnClash) {
                                            viewModel.updateUserInfo {
                                                launch { startClash() }
                                                viewModel.guestConnClash = false
                                            }
                                        } else {
                                            startClash()
                                        }
                                    }
                                }
                            }

                            HomeDesign.Request.OpenProxy ->
                                startActivity(ProxyActivity::class.intent)

                            HomeDesign.Request.ProxyRule ->
                                updateProxyMode(TunnelState.Mode.Rule)

                            HomeDesign.Request.ProxyGlobal ->
                                updateProxyMode(TunnelState.Mode.Global)
                        }
                    }
                    if (clashRunning) {
                        ticker.onReceive {
                            design.fetchTraffic()
                        }
                    }
                }
            }
        }
    }

    private suspend fun updateProxyMode(mode: TunnelState.Mode) {
        withClash {
            val o = queryOverride(Clash.OverrideSlot.Session)
            o.mode = mode
            patchOverride(Clash.OverrideSlot.Session, o)
        }
        design.setMode(mode)
        context?.toast(R.string.mode_switch_tips)
        delay(600)
    }

    private fun initObserver() {
        viewModel.lgnState.observe(viewLifecycleOwner) {
            refreshSubsInfo = true
            Logger.i("changed the login state:$it")
            if (it) {
                hasReqInitMsg = true
                viewModel.updateUserInfo()
                viewModel.fetchSubscribeInfo()
                viewModel.fetchNoticeInfo()
            }
        }
        viewModel.subsInfo.observe(viewLifecycleOwner) {
            if (it == null || it.subscribe_url.isEmpty()) {
                return@observe
            }
            fetchProfile(it.subscribe_url)
            refreshSubsInfo = false
        }
        viewModel.appConfig.observe(viewLifecycleOwner) {
            if (!viewModel.userHasLogin) {
                viewModel.fetchSubscribeInfo()
                viewModel.fetchNoticeInfo()
            }
        }
        viewModel.noticeMsgList.observe(viewLifecycleOwner) {
            design.initNoticeView(it)
        }

        ViewModelManager.appVM.selectProxyName.observe(viewLifecycleOwner) {
            if (it.isNullOrBlank()) return@observe
            launch {
                design.setProxyName(it)
                Logger.i("Update node to:${it}")
            }
        }

    }

    private fun fetchProfile(url: String) {
        launch {
            Logger.i("fetchProfile->given url:$url")
            withProfile {
                val serviceStore = ServiceStore(activity)
                val savedProf = queryActive()
                Logger.d("fetchProfile savedProf:$savedProf")
                if (savedProf == null) {
                    val name = getString(designR.string.new_profile)
                    val uuid: UUID = create(Profile.Type.Url, name)

                    val originProf = queryByUUID(uuid) ?: return@withProfile
                    val profile = originProf.copy(source = url)
                    load(profile)
                    serviceStore.dynamicSubsUrl = url
                    activity.defer {
                        release(uuid)
                    }
                } else {
                    Logger.d("fetchProfile dynamic url:${serviceStore.dynamicSubsUrl}")
                    if (url == serviceStore.dynamicSubsUrl) {
                        val store = TipsStore(activity)
                        val last = store.updateProfTime
                        if (System.currentTimeMillis() - last > 10 * 60 * 1000) {
                            update(savedProf.uuid)
                            store.updateProfTime = System.currentTimeMillis()
                        }
                    } else {
                        val updateProf = savedProf.copy(source = url)
                        load(updateProf)
                        serviceStore.dynamicSubsUrl = url
                    }
                }
            }
        }
    }

    private fun load(profile: Profile) {
        try {
            Logger.d("load profile source:${profile.source}")
            withProcessing { updateStatus ->
                withProfile {
                    patch(profile.uuid, profile.name, profile.source, profile.interval)

                    commit(profile.uuid) {
                        launch {
                            updateStatus(it)
                        }
                        Logger.i("commit profile:${profile.uuid}, FetchStat progress:${it.progress}")
                    }
                    Logger.i("after commit:${profile.uuid}")
                    updateStatus(
                        FetchStatus(
                            action = FetchStatus.Action.FetchProviders,
                            emptyList(), 1, 10
                        )
                    )
                    setActive(profile)
                    updateStatus(null)
                    Logger.d("active profile source:${profile.source}")
                }
            }
        } catch (e: Exception) {
            Logger.e("load Profile exception:${e.message}")
            e.printStackTrace()
        }
    }

    private fun withProcessing(executeTask: suspend (suspend (FetchStatus?) -> Unit) -> Unit) {
        try {
            launch(Dispatchers.Main) {
                activity.showModalProgressBar {
                    configure {
                        isIndeterminate = true
                        text = getString(designR.string.initializing)
                    }

                    executeTask {
                        if (it == null) {
                            onResult()
                            return@executeTask
                        }
                        configure {
                            applyFrom(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("load Profile exception:${e.message}")
            e.printStackTrace()
        }
    }

    private fun ModelProgressBarConfigure.applyFrom(status: FetchStatus) {
        when (status.action) {
            FetchStatus.Action.FetchConfiguration -> {
                text = getString(designR.string.format_fetching_configuration)
                isIndeterminate = true
            }

            FetchStatus.Action.Verifying -> {
                text = getString(designR.string.verifying)
                isIndeterminate = true
            }

            else -> {
                text = "激活订阅地址"
                isIndeterminate = true
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    private suspend fun HomeDesign.fetch() {
        setClashRunning(clashRunning)

        val state = withClash {
            queryTunnelState()
        }
        val providers = withClash {
            queryProviders()
        }

        setMode(state.mode)
        setHasProviders(providers.isNotEmpty())

        if (clashRunning) {
            val groups = withClash { queryProxyGroupNames(true) }
            groups.takeIf { it.isNotEmpty() }?.let {
                val group = withClash {
                    queryProxyGroup(it[0], ProxySort.Default)
                }
                var currNodeName = group.now
                Logger.i("current mode is:${state.mode.name}, Node initialized to:${currNodeName}")
                if (currNodeName.isEmpty() && group.proxies.size > 1) {
                    val proxy = group.proxies[group.proxies.size / 2]
                    currNodeName = proxy.name
                    withClash { patchSelector(groups[0], proxy.name) }
                }
                ViewModelManager.appVM.selectProxyName.value = currNodeName
            }
        }
        withProfile {
            setProfileName(queryActive()?.name)
        }
    }

    private suspend fun HomeDesign.fetchTraffic() {
        withClash {
            setForwarded(queryTrafficTotal())
        }
    }

    private suspend fun startClash() {
        //检查用户套餐是否已过期
        /*val userInfo = DataRepository.globalDS().getValue("key_user_info", UserInfo::class)
        userInfo.apply {
            val usIf = first()
            Logger.d("startClash->dataStore cache:${usIf.toString()}")
            val userInfo = GsonHelper.parseBean(usIf.toString(), UserInfo::class.java)
            Logger.i("startClash->dataStore user email:${userInfo?.email}, expireAt:${userInfo?.expired_at?.toDateStr()}")
        }*/
        Logger.i("startClash->current userInfo.value is:${viewModel.userInfo.value}")
        /*viewModel.userInfo.value?.apply {
            Logger.i("the current login user mail:$email, hasLogin:${viewModel.userHasLogin}, expireAt:${expired_at.toDateStr()}")
            if (viewModel.userHasLogin && TimeFormat.isExpireAt(expired_at)) {
                CommonDialog.show((context as AppCompatActivity).supportFragmentManager) {
                    onlyConfirm = true
                    content = "您的流量套餐已到期，请购买新的套餐后使用。"
                }
                return
            }
        }*/
        val active = withProfile { queryActive() }

        if (active == null || !active.imported) {
            Logger.e("startClash active:$active")
            activity.toast(R.string.no_profile_selected)
            return
        }

        val vpnRequest = activity.startClashService()

        try {
            if (vpnRequest != null) {
                val result = activity.startActivityForResult(
                    ActivityResultContracts.StartActivityForResult(),
                    vpnRequest
                )

                if (result.resultCode == AppCompatActivity.RESULT_OK)
                    activity.startClashService()
            }
        } catch (e: Exception) {
            design.showToast(R.string.unable_to_start_vpn, ToastDuration.Long)
        }
    }

}