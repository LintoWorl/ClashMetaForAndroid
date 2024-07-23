package com.github.kr328.clash.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.common.compat.checkEmpty
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.adapter.MailAddressAdapter
import com.github.kr328.clash.design.databinding.FragRegisterAccountBinding
import com.github.kr328.clash.design.dialog.showModalProgressBar
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private lateinit var binding: FragRegisterAccountBinding
    private val viewModel by activityViewModels<MainViewModel>()
    private var mailSuffix: String = "@gmail.com"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragRegisterAccountBinding.inflate(inflater, container, false)
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
        binding.titleBar.titleBarText.text = "注册"
        binding.mailList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                binding.mailList.setSelection(position)
                mailSuffix = "@${binding.mailList.selectedItem}"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        binding.btnRegister.onClickNew {
            if (binding.editEmail.checkEmpty("请输入邮箱")
                || binding.editPassword.checkEmpty("请输入密码")
                || binding.editVerifyCode.checkEmpty("请输入验证码")
                || !agreePolicies()
            ) {
                return@onClickNew
            }
            val mailAddress = binding.editEmail.text.toString() + mailSuffix
            val password = binding.editPassword.text.toString()
            val mailCode = binding.editVerifyCode.text.toString()
            val inviteCode = binding.editInviteCode.text.toString()

            CoroutineScope(Dispatchers.Main).launch {
                requireContext().showModalProgressBar {
                    configure {
                        isIndeterminate = true
                        text = "提交注册，请稍候..."
                    }
                    //调注册用户的API
                    RequestHandler.request({
                        UserAccountApi.registerAccount(mailAddress, password, mailCode, inviteCode)
                    }, {
                        Logger.d("init guest config data:$it")
                        onResult()
                        val appStore = AppStore(requireContext())
                        appStore.userToken = it.token
                        appStore.authData = it.auth_data
                        //TODO 提示用户注册成功，直接进入首页
                        viewModel.fragIndex.value = MainViewModel.IDX_FRAG_HOME
                    }, { _, msg ->
                        context?.toast(msg)
                        onResult()
                    })
                }
            }
        }

        binding.titleBar.titleBarGoback.onClickNew {
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
        }
        binding.btnSendCode.onClickNew {
            if (binding.editEmail.checkEmpty("请输入邮箱")) return@onClickNew
            val mailAddress = binding.editEmail.text.toString() + mailSuffix
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
            binding.editPassword.inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.editPassword.setSelection(binding.editPassword.text.toString().length)
        }
    }

    private fun agreePolicies(): Boolean {
        if (!binding.cbAgreeTos.isChecked) {
            context?.toast("请阅读并同意隐私政策和用户协议")
            return false
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance() = RegisterFragment()
    }
}