package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.kr328.clash.AppSettingsActivity
import com.github.kr328.clash.LogsActivity
import com.github.kr328.clash.MetaFeatureSettingsActivity
import com.github.kr328.clash.NetworkSettingsActivity
import com.github.kr328.clash.OverrideSettingsActivity
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.packageName
import com.github.kr328.clash.core.bridge.Bridge
import com.github.kr328.clash.design.SettingsDesign
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class UserFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var design: SettingsDesign
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        design = SettingsDesign(requireActivity())
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
    }

    private fun initView() {
        launch {
            while (isActive) {
                select {
                    design.requests.onReceive {
                        when (it) {
                            SettingsDesign.Request.StartApp ->
                                startActivity(AppSettingsActivity::class.intent)

                            SettingsDesign.Request.StartNetwork ->
                                startActivity(NetworkSettingsActivity::class.intent)

                            SettingsDesign.Request.StartOverride ->
                                startActivity(OverrideSettingsActivity::class.intent)

                            SettingsDesign.Request.StartMetaFeature ->
                                startActivity(MetaFeatureSettingsActivity::class.intent)

                            SettingsDesign.Request.OpenLogs ->
                                startActivity(LogsActivity::class.intent)

                            SettingsDesign.Request.OpenAbout ->
                                design.showAbout(queryAppVersionName())
                        }
                    }
                }
            }
        }
    }

    private suspend fun queryAppVersionName(): String {
        return withContext(Dispatchers.IO) {
            requireContext().packageManager.getPackageInfo(
                packageName, 0
            ).versionName + "\n" + Bridge.nativeCoreVersion().replace("_", "-")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserFragment()
    }
}