package com.github.kr328.clash.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.MainV2Activity
import com.github.kr328.clash.SettingsActivity
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.common.util.TimeFormat
import com.github.kr328.clash.common.util.TimeFormat.FORMAT_YYYY_MM_DD
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.design.databinding.FragUserCenterBinding
import com.github.kr328.clash.design.dialog.showModalProgressBar
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class UserFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragUserCenterBinding

    //private lateinit var design: SettingsDesign
    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var activity: MainV2Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //design = SettingsDesign(requireActivity())
        activity = requireActivity() as MainV2Activity
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
        fetchUserInfo()
        initObserver()
    }

    private fun initView() {
        binding.btnLogout.onClickNew {
            val appStore = AppStore(activity)
            appStore.hasLoginApp = false
            appStore.authData = ""
            context?.toast("退出登录成功")
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
            //请求退出登录API
            RequestHandler.request({
                UserAccountApi.logout()
            }, {
            }, { _, msg ->
                Logger.e("Logout fail:$msg")
            })
        }
        binding.btnResetPwd.onClickNew {
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_XPWD
        }
        //TODO
        binding.btnSetting.onClickNew {
            startActivity(SettingsActivity::class.intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchUserInfo() {
        launch {
            context?.showModalProgressBar {
                configure {
                    isIndeterminate = true
                    text = "更新数据，请稍候..."
                }
                RequestHandler.request({
                    UserAccountApi.userAccountInfo()
                }, {
                    Logger.d("got userInfo:${it.email}")//TODO
                    binding.tvAccountEmail.text = it.email
                    val planExpire = TimeFormat.millis2String(it.expired_at, FORMAT_YYYY_MM_DD)
                    binding.tvSubsDesc.text = "套餐到期时间：${planExpire}"
                    val loginTime = TimeFormat.millis2String(it.last_login_at, FORMAT_YYYY_MM_DD)
                    binding.tvLastLogin.text = "上次登录时间：${loginTime}"
                    onResult()
                }, { code, msg ->
                    context?.toast(msg)
                    onResult()
                })
            }
        }
    }

    private fun initObserver() {
        viewModel.subsInfo.observe(viewLifecycleOwner) {
            val subsPlan = it?.plan ?: return@observe
            binding.tvSubsTitle.text = subsPlan.name
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserFragment()
    }
}