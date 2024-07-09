package com.github.kr328.clash.fragment

import android.content.Context
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
import com.github.kr328.clash.ProvidersActivity
import com.github.kr328.clash.ProxyActivity
import com.github.kr328.clash.R
import com.github.kr328.clash.SettingsActivity
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.ticker
import com.github.kr328.clash.design.HomeDesign
import com.github.kr328.clash.design.ui.ToastDuration
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.util.withClash
import com.github.kr328.clash.util.withProfile
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var design: HomeDesign
    private lateinit var context: Context
    private val viewModel by activityViewModels<MainViewModel>()

    val clashRunning: Boolean
        get() = Remote.broadcasts.clashRunning

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = requireActivity()
        design = HomeDesign(context)
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
        initView()
        main()
    }

    private fun initView() {

    }

    fun main() {
        launch {
            design.fetch()
            val ticker = ticker(TimeUnit.SECONDS.toMillis(1))

            while (isActive) {
                select {
                    (context as MainV2Activity).events.onReceive {
                        when (it) {
                            BaseActivity.Event.ActivityStart, BaseActivity.Event.ServiceRecreated,
                            BaseActivity.Event.ClashStop, BaseActivity.Event.ClashStart,
                            BaseActivity.Event.ProfileLoaded, BaseActivity.Event.ProfileChanged -> {
                                design.fetch()
                            }

                            else -> Unit
                        }
                    }
                    design.requests.onReceive {
                        when (it) {
                            HomeDesign.Request.ToggleStatus -> {
                                if (clashRunning)
                                    context.stopClashService()
                                else
                                    design.startClash()
                            }

                            HomeDesign.Request.OpenProxy ->
                                startActivity(ProxyActivity::class.intent)

//                            HomeDesign.Request.OpenProviders ->
//                                startActivity(ProvidersActivity::class.intent)
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

        val vpnRequest = context.startClashService()

        try {
            if (vpnRequest != null) {
                val result = (context as MainV2Activity).startActivityForResult(
                    ActivityResultContracts.StartActivityForResult(),
                    vpnRequest
                )

                if (result.resultCode == AppCompatActivity.RESULT_OK)
                    context.startClashService()
            }
        } catch (e: Exception) {
            design.showToast(R.string.unable_to_start_vpn, ToastDuration.Long)
        }
    }

}