package com.github.kr328.clash.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.common.compat.checkEmpty
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
            if (binding.editEmail.checkEmpty("请输入邮箱")
                || binding.editVerifyCode.checkEmpty("请输入验证码")
                || binding.editNewPwd.checkEmpty("请输入新密码")
                || binding.confirmNewPwd.checkEmpty("请确认新密码")
            ) {
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
            if (binding.editEmail.checkEmpty("请输入邮箱")) {
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

        binding.cbShowPwd.setOnCheckedChangeListener { _, isChecked ->
            binding.editNewPwd.inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.editNewPwd.setSelection(binding.editNewPwd.text.toString().length)
        }
        binding.cbShowConfirmPwd.setOnCheckedChangeListener { _, isChecked ->
            binding.confirmNewPwd.inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.confirmNewPwd.setSelection(binding.confirmNewPwd.text.toString().length)
        }
        //校验两次输入的密码是否一致
        binding.editNewPwd.addTextChangedListener {
            it ?: return@addTextChangedListener
            val confirm = binding.confirmNewPwd.text
            if (confirm.isNullOrEmpty()) return@addTextChangedListener
            if (it.toString() != confirm.toString()) {
                binding.editNewPwd.error = "请保持两次输入密码一致"
            }
        }
        binding.confirmNewPwd.addTextChangedListener {
            it ?: return@addTextChangedListener
            val newPwd = binding.editNewPwd.text
            if (newPwd.isNullOrEmpty()) return@addTextChangedListener
            if (it.toString() != newPwd.toString()) {
                binding.confirmNewPwd.error = "请保持两次输入密码一致"
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ResetPwdFragment()
    }
}