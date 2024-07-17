package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.UrlConnManager
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import app.hw.network.model.Constant.PROTOCOL_HTTPS
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.log.Log.TAG_HTTP
import com.github.kr328.clash.design.databinding.FragLoginAccountBinding
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class LoginFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragLoginAccountBinding
    private val viewModel by activityViewModels<MainViewModel>()

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

        launch {
            while (isActive) {
                select {
                    Global.commEvents.onReceive {
                        initData()
                        //fetchData()
                    }
                }
            }
        }

    }

    private fun initView() {
        binding.btnLoginAccount.onClickNew {
            //用账号登录
            RequestHandler.request({
                UserAccountApi.login("hieye@qq.com", "sdwe12324")
            }, {
                Log.d("Login success:$it")
                viewModel.fragIndex.value = MainViewModel.IDX_FRAG_HOME
                AppStore(requireContext()).hasLoginApp = true
            }, { _, msg ->
                Log.e("initData fail: $msg")
            })
        }
        binding.btnEnterTourist.onClickNew {
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

    private fun initData() {
        RequestHandler.request({
            UserAccountApi.appConfig()
        }, {
            Log.d("init guest config data:$it")
        }, { _, msg ->
            Log.e("initData fail: $msg")
        })
    }

    private suspend fun fetchData() {
        coroutineScope {
            launch(Dispatchers.IO) {
                val result =
                    UrlConnManager.getUrlContentV2("/api/v1/guest/comm/config")
                    //UrlConnManager.getUrlContent("${PROTOCOL_HTTPS}a1.8jiasu.com/api/v1/guest/comm/config")
                android.util.Log.d(TAG_HTTP,"getUrlContent:$result")
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}