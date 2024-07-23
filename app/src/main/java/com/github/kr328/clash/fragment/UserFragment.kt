package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.databinding.FragUserCenterBinding
import com.github.kr328.clash.design.dialog.showModalProgressBar
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class UserFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragUserCenterBinding

    //private lateinit var design: SettingsDesign
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //design = SettingsDesign(requireActivity())
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
    }

    private fun initView() {
        binding.btnLogout.onClickNew {
            //请求退出登录API
            RequestHandler.request({
                UserAccountApi.logout()
            }, {
                if (it) {
                    context?.toast("退出登录成功")
                    viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
                }
            }, { _, msg ->
                context?.toast(msg)
            })
        }
        binding.btnResetPwd.onClickNew {
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_REPWD
        }
        binding.tvSubsTitle.text = "套餐名称"
        //TODO
    }

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
                    onResult()
                }, { code, msg ->
                    context?.toast(msg)
                    onResult()
                })
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserFragment()
    }
}