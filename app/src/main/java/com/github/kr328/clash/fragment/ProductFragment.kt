package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.kr328.clash.BaseActivity
import com.github.kr328.clash.NewProfileActivity
import com.github.kr328.clash.PropertiesActivity
import com.github.kr328.clash.R
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.common.util.ticker
import com.github.kr328.clash.design.ProfilesDesign
import com.github.kr328.clash.design.databinding.FragUserCenterBinding
import com.github.kr328.clash.design.ui.ToastDuration
import com.github.kr328.clash.remote.Broadcasts
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.util.withProfile
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
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
    private val events = Channel<BaseActivity.Event>(Channel.UNLIMITED)
    private var activityStarted: Boolean = false
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        design = ProfilesDesign(requireContext())
    }

    override fun onStart() {
        super.onStart()
        activityStarted = true
        events.trySend(BaseActivity.Event.ActivityStart)
    }

    override fun onStop() {
        super.onStop()
        activityStarted = false
        events.trySend(BaseActivity.Event.ActivityStop)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //binding = FragUserCenterBinding.inflate(inflater, container, false)
        return design.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        launch {
            val ticker = ticker(TimeUnit.MINUTES.toMillis(1))
            while (isActive) {
                select<Unit> {
                    events.onReceive {
                        when (it) {
                            BaseActivity.Event.ActivityStart, BaseActivity.Event.ProfileChanged -> {
                                design.fetch()
                            }

                            else -> Unit
                        }
                    }
                    design.requests.onReceive {
                        when (it) {
                            ProfilesDesign.Request.Create ->
                                startActivity(NewProfileActivity::class.intent)

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
                    if (activityStarted) {
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

    }

    override fun onProfileUpdateCompleted(uuid: UUID?) {
        if (uuid == null)
            return
        launch {
            var name: String? = null
            withProfile {
                name = queryByUUID(uuid)?.name
            }
            design?.showToast(
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

    companion object {
        @JvmStatic
        fun newInstance() = ProductFragment()
    }
}