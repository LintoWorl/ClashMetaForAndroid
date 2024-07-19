package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.databinding.FragResetPasswordBinding
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.vm.MainViewModel

class ResetPwdFragment : Fragment() {
    private lateinit var binding: FragResetPasswordBinding
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.titleBar.titleBarText.text = "重置密码"
        binding.btnResetPwd.onClickNew {
            //调重置用户密码的API
            val mailCode = binding.editVerifyCode.text.toString()

        }
        binding.titleBar.titleBarGoback.onClickNew {
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
        }
        binding.btnSendCode.onClickNew {
            val mailAddress = binding.editEmail.text.toString()
            if (binding.editEmail.text.isNullOrEmpty()) {
                context?.toast("请输入邮箱")
                return@onClickNew
            }
            RequestHandler.request({
                UserAccountApi.sendEmailVerifyCode(mailAddress)
            }, {

            }, { code, msg ->

            })
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ResetPwdFragment()
    }
}