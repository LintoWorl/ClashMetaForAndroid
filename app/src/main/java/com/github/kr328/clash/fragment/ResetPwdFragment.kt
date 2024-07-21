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
            if (!validMail() || !validMailCode() || !validNewPwd() || !validNewPwd2()) {
                return@onClickNew
            }
            //调重置用户密码的API
            val mailAddress = binding.editEmail.text.toString()
            val password = binding.editNewPwd.text.toString()
            val mailCode = binding.editVerifyCode.text.toString()
            RequestHandler.request({
                UserAccountApi.forgetAccount(mailAddress, password, mailCode)
            }, {
                if (it) {
                    context?.toast("重置密码成功")
                    viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
                } else {
                    context?.toast("重置密码失败，请稍后重试")
                }
            }, { _, msg ->
                context?.toast(msg)
            })

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
                if (it) {
                    context?.toast("验证码发送成功，请在5分钟内使用该验证码")
                } else {
                    context?.toast("验证码发送失败")
                }
            }, { code, msg ->
                context?.toast(msg)
            })
        }

        //校验两次输入的密码是否一致 TODO
    }

    private fun validMail(): Boolean {
        if (binding.editEmail.text.isNullOrEmpty()) {
            context?.toast("请输入邮箱")
            return false
        }
        return true
    }

    private fun validNewPwd(): Boolean {
        if (binding.editNewPwd.text.isNullOrEmpty()) {
            context?.toast("请输入新密码")
            return false
        }
        return true
    }

    private fun validNewPwd2(): Boolean {
        if (binding.confirmNewPwd.text.isNullOrEmpty()) {
            context?.toast("请确认新密码")
            return false
        }
        return true
    }

    private fun validMailCode(): Boolean {
        if (binding.editVerifyCode.text.isNullOrEmpty()) {
            context?.toast("请输入验证码")
            return false
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance() = ResetPwdFragment()
    }
}