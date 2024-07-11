package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.kr328.clash.BaseActivity
import com.github.kr328.clash.MainV2Activity
import com.github.kr328.clash.PropertiesActivity
import com.github.kr328.clash.R
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.common.util.ticker
import com.github.kr328.clash.design.ProfilesDesign
import com.github.kr328.clash.design.ui.ToastDuration
import com.github.kr328.clash.design.util.showExceptionToast
import com.github.kr328.clash.remote.Broadcasts
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.util.withProfile
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class ProductFragment : Fragment(), CoroutineScope by MainScope(), Broadcasts.Observer {

    private lateinit var design: ProfilesDesign
    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var activity: MainV2Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = requireActivity() as MainV2Activity
        design = ProfilesDesign(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //binding = FragUserCenterBinding.inflate(inflater, container, false)
        Remote.broadcasts.addObserver(this)
        return design.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Remote.broadcasts.removeObserver(this)
    }

    override fun onDestroy() {
        design.cancel()
        cancel()
        super.onDestroy()
    }

    private fun initView() {
        launch {
            design.fetch()//初始化页面数据
            val ticker = ticker(TimeUnit.MINUTES.toMillis(1))
            while (isActive) {
                select {
                    activity.events.onReceive {
                        when (it) {
                            BaseActivity.Event.ProfileChanged -> {
                                design.fetch()
                            }

                            else -> Unit
                        }
                    }
                    design.requests.onReceive {
                        when (it) {
                            ProfilesDesign.Request.Create ->
                                withProfile {
                                    //val name = getString(R.string.new_profile)
                                    val name = "default_profile"
                                    val uuid: UUID = create(Profile.Type.Url, name)
                                    //launchProperties(uuid)

                                    val originProf =
                                        withProfile { queryByUUID(uuid) } ?: return@withProfile
                                    val profile =
                                        originProf.copy(source = "https://s.jiasu01.vip/bd/api/v1/client/subscribe?token=07d0d5175bf41ae8c335128597e20e9e&flag=clash")
                                    try {
                                        //design.withProcessing { updateStatus ->
                                        design.showProgress(true)
                                        withProfile {
                                            patch(
                                                profile.uuid,
                                                profile.name,
                                                profile.source,
                                                profile.interval
                                            )

                                            coroutineScope {
                                                commit(profile.uuid) {
                                                    launch {
                                                        //updateStatus(it)
                                                        design.showProgress(false)
                                                    }
                                                }
                                            }
                                        }
                                        //}
                                    } catch (e: Exception) {
                                        design.showExceptionToast(e)
                                        design.showProgress(false)
                                    }
                                }

                            ProfilesDesign.Request.UpdateAll ->
                                withProfile {
                                    try {
                                        queryAll().forEach { p ->
                                            if (p.imported && p.type != Profile.Type.File)
                                                update(p.uuid)
                                        }
                                    } finally {
                                        withContext(Dispatchers.Main) {
                                            design.finishUpdateAll();
                                        }
                                    }
                                }

                            is ProfilesDesign.Request.Update ->
                                withProfile { update(it.profile.uuid) }

                            is ProfilesDesign.Request.Delete ->
                                withProfile { delete(it.profile.uuid) }

                            is ProfilesDesign.Request.Edit ->
                                startActivity(PropertiesActivity::class.intent.setUUID(it.profile.uuid))

                            is ProfilesDesign.Request.Active -> {
                                withProfile {
                                    if (it.profile.imported)
                                        setActive(it.profile)
                                    else
                                        design.requestSave(it.profile)
                                }
                            }

                            is ProfilesDesign.Request.Duplicate -> {
                                val uuid = withProfile { clone(it.profile.uuid) }

                                startActivity(PropertiesActivity::class.intent.setUUID(uuid))
                            }
                        }
                    }
                    if (activity.activityStarted) {
                        ticker.onReceive {
                            design.updateElapsed()
                        }
                    }
                }
            }
        }
    }

    private suspend fun ProfilesDesign.fetch() {
        withProfile {
            patchProfiles(queryAll())
        }
    }

    override fun onServiceRecreated() {

    }

    override fun onStarted() {

    }

    override fun onStopped(cause: String?) {

    }

    override fun onProfileChanged() {
        activity.events.trySend(BaseActivity.Event.ProfileChanged)
    }

    override fun onProfileUpdateCompleted(uuid: UUID?) {
        if (uuid == null)
            return
        launch {
            var name: String? = null
            withProfile {
                name = queryByUUID(uuid)?.name
            }
            design.showToast(
                getString(R.string.toast_profile_updated_complete, name),
                ToastDuration.Long
            )
        }
    }

    override fun onProfileUpdateFailed(uuid: UUID?, reason: String?) {
        if (uuid == null)
            return
        launch {
            var name: String? = null
            withProfile {
                name = queryByUUID(uuid)?.name
            }
            design.showToast(
                getString(R.string.toast_profile_updated_failed, name, reason),
                ToastDuration.Long
            ) {
                setAction(R.string.edit) {
                    startActivity(PropertiesActivity::class.intent.setUUID(uuid))
                }
            }
        }
    }

    override fun onProfileLoaded() {

    }

    private fun launchProperties(uuid: UUID) {
        startActivity(PropertiesActivity::class.intent.setUUID(uuid))
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProductFragment()
    }
}