package com.github.kr328.clash.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.kr328.clash.common.compat.checkEmpty
import com.github.kr328.clash.common.constants.Authorities
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.databinding.FragLoginAccountBinding
import com.github.kr328.clash.design.dialog.showModalProgressBar
import com.github.kr328.clash.design.util.hideKeyboard
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LoginFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragLoginAccountBinding
    private val viewModel by activityViewModels<MainViewModel>()
    private var tosUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragLoginAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserve()
    }

    private fun initView() {
        binding.root.onClickNew { it.hideKeyboard() }
        binding.btnLoginAccount.onClickNew {
            if (binding.editEmail.checkEmpty("请输入邮箱地址")
                || binding.editPassword.checkEmpty("请输入密码")
                || !agreePolicies()
            ) return@onClickNew

            it.hideKeyboard()
            handleLogin()
        }

        binding.btnEnterTourist.onClickNew {
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_HOME
            viewModel.loginApp("peterpan168@qq.com", "pass.jsb.8812",
                onSucc = { lgn ->
                    val appStore = AppStore(requireContext())
                    appStore.userToken = lgn.token
                    appStore.authData = lgn.auth_data
                    Authorities.authData = lgn.auth_data
                    appStore.hasLoginApp = false
                }, onFail = {})
        }

        binding.btnEnterRegister.onClickNew {
            if (viewModel.appConfig.value == null) {
                viewModel.initConfigs(requireActivity())
            }
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_REGST
        }

        binding.tvForgetPwd.onClickNew {
            //重置密码
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_REPWD
        }

        binding.cbShowPwd.setOnCheckedChangeListener { _, isChecked ->
            binding.editPassword.inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.editPassword.setSelection(binding.editPassword.text.toString().length)
        }
    }

    private fun initObserve() {
        viewModel.appConfig.observe(viewLifecycleOwner) {
            tosUrl = it.tos_url
            AppStore(requireContext()).tosAddress = it.tos_url
        }
    }

    private fun agreePolicies(): Boolean {
        if (!binding.cbAgreeTos.isChecked) {
            context?.toast("请阅读并同意隐私政策和用户协议")
            return false
        }
        return true
    }

    private fun handleLogin() {
        launch {
            requireContext().showModalProgressBar {
                configure {
                    isIndeterminate = true
                    text = "登录中，请稍候..."
                }
                //用账号登录
                viewModel.loginApp(
                    binding.editEmail.text.toString(),
                    binding.editPassword.text.toString(),
                    { lgn ->
                        onResult()
                        viewModel.fragIndex.value = MainViewModel.IDX_FRAG_HOME
                        val appStore = AppStore(requireContext())
                        appStore.userToken = lgn.token
                        appStore.authData = lgn.auth_data
                        Authorities.authData = lgn.auth_data
                        appStore.hasLoginApp = true
                    },
                    { msg ->
                        onResult()
                        requireContext().toast(msg)
                    })
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}