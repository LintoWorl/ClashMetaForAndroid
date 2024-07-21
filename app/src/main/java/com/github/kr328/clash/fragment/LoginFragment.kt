package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.UrlConnManager
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.log.Logger.TAG_HTTP
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.adapter.MailAddressAdapter
import com.github.kr328.clash.design.databinding.FragLoginAccountBinding
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class LoginFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragLoginAccountBinding
    private val viewModel by activityViewModels<MainViewModel>()
    private var mailSuffix: String = "@gmail.com"

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
        viewModel.appConfig.observe(viewLifecycleOwner) {
            it ?: return@observe
            val mailList = it.email_whitelist_suffix
            binding.mailList.adapter = MailAddressAdapter(requireContext(), mailList)
        }
    }

    private fun initView() {
        binding.mailList.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                binding.mailList.setSelection(position)
                mailSuffix = "@${binding.mailList.selectedItem}"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        binding.btnLoginAccount.onClickNew {
            //用账号登录
            RequestHandler.request({
                val mailAddress = binding.editEmail.text.toString() + mailSuffix
                UserAccountApi.login(mailAddress, binding.editPassword.text.toString())
            }, {
                Logger.d("init guest config data:$it")
                val appStore = AppStore(requireContext())
                appStore.userToken = it.token
                appStore.authData = it.auth_data
                viewModel.fragIndex.value = MainViewModel.IDX_FRAG_HOME
                appStore.hasLoginApp = true
                viewModel.fetchSubsPlan(false)
            }, { code, msg ->
                context?.toast(msg)
            })
        }
        binding.btnEnterTourist.onClickNew {
            viewModel.fetchSubsPlan(true)
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_HOME
        }
        binding.btnEnterRegister.onClickNew {
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_REGST
        }
        binding.tvForgetPwd.onClickNew {
            //重置密码
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_REPWD
        }
    }

    private suspend fun fetchData() {
        coroutineScope {
            launch(Dispatchers.IO) {
                val result =
                    UrlConnManager.getUrlContentV2("/api/v1/guest/comm/config")
                //UrlConnManager.getUrlContent("${PROTOCOL_HTTPS}a1.8jiasu.com/api/v1/guest/comm/config")
                android.util.Log.d(TAG_HTTP, "getUrlContent:$result")
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}