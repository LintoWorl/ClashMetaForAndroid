package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.kr328.clash.BaseActivity
import com.github.kr328.clash.MainV2Activity
import com.github.kr328.clash.ProfilesActivity
import com.github.kr328.clash.ProxyActivity
import com.github.kr328.clash.R
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.ticker
import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.design.HomeDesign
import com.github.kr328.clash.design.dialog.ModelProgressBarConfigure
import com.github.kr328.clash.design.dialog.withModelProgressBar
import com.github.kr328.clash.design.ui.ToastDuration
import com.github.kr328.clash.remote.Broadcasts
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.util.subsUrl
import com.github.kr328.clash.util.withClash
import com.github.kr328.clash.util.withProfile
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment(), CoroutineScope by MainScope(), Broadcasts.Observer {

    private lateinit var design: HomeDesign
    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var activity: MainV2Activity

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
        Remote.broadcasts.addObserver(this)
        return design.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main()
        viewModel.fetchSubscribeInfo()
        initObserver()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            Remote.broadcasts.removeObserver(this)
        } else {
            Remote.broadcasts.addObserver(this)
        }
    }

    fun main() {
        launch {
            //design.fetch()
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
                                if (clashRunning)
                                    activity.stopClashService()
                                else
                                    design.startClash()
                            }

                            HomeDesign.Request.OpenProxy ->
                                startActivity(ProxyActivity::class.intent)
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

    private fun initObserver() {
        viewModel.subsInfo.observe(viewLifecycleOwner) {
            if (it == null || it.subscribe_url.isEmpty()) {
                return@observe
            }
            fetchProfile(it.subscribe_url)
        }
    }

    private fun fetchProfile(url: String) {
        launch {
            withProfile {
                val savedProf = queryActive()
                if (savedProf == null) {
                    val name = getString(com.github.kr328.clash.design.R.string.new_profile)
                    //val name = "default_profile"
                    val uuid: UUID = create(Profile.Type.Url, name)

                    val originProf = queryByUUID(uuid) ?: return@withProfile
                    val profile = originProf.copy(source = url)
                    load(profile)
                    activity.defer {
                        release(uuid)
                    }
                } else {
                    update(savedProf.uuid)
                }
                //delay(3000)
            }
        }
    }

    private fun load(profile: Profile) {
        try {
            withProcessing { updateStatus ->
                withProfile {
                    patch(profile.uuid, profile.name, profile.source, profile.interval)

                    coroutineScope {
                        commit(profile.uuid) {
                            launch {
                                updateStatus(it)
                            }
                        }
                    }
                }
                withProfile {
                    setActive(profile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun withProcessing(executeTask: suspend (suspend (FetchStatus) -> Unit) -> Unit) {
        try {
            launch(Dispatchers.Main) {
                activity.withModelProgressBar {
                    configure {
                        isIndeterminate = true
                        text = getString(com.github.kr328.clash.design.R.string.initializing)
                    }

                    executeTask {
                        configure {
                            applyFrom(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun ModelProgressBarConfigure.applyFrom(status: FetchStatus) {
        when (status.action) {
            FetchStatus.Action.FetchConfiguration -> {
                text = getString(
                    com.github.kr328.clash.design.R.string.format_fetching_configuration,
                    status.args[0]
                )
                isIndeterminate = true
            }

            FetchStatus.Action.Verifying -> {
                text = getString(com.github.kr328.clash.design.R.string.verifying)
                isIndeterminate = false
                max = status.max
                progress = status.progress
            }

            else -> {}
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

        withProfile {
            setProfileName(queryActive()?.name)
        }
    }

    private suspend fun HomeDesign.fetchTraffic() {
        withClash {
            setForwarded(queryTrafficTotal())
        }
    }

    private suspend fun HomeDesign.startClash() {
        val active = withProfile { queryActive() }

        if (active == null || !active.imported) {
            showToast(R.string.no_profile_selected, ToastDuration.Long) {
                setAction(R.string.profiles) {
                    startActivity(ProfilesActivity::class.intent)
                }
            }

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

    override fun onServiceRecreated() {
        activity.events.trySend(BaseActivity.Event.ServiceRecreated)
    }

    override fun onStarted() {
        activity.events.trySend(BaseActivity.Event.ClashStart)
    }

    override fun onStopped(cause: String?) {
        activity.events.trySend(BaseActivity.Event.ClashStop)
    }

    override fun onProfileChanged() {
        activity.events.trySend(BaseActivity.Event.ProfileLoaded)
    }

    override fun onProfileUpdateCompleted(uuid: UUID?) {

    }

    override fun onProfileUpdateFailed(uuid: UUID?, reason: String?) {

    }

    override fun onProfileLoaded() {

    }

}