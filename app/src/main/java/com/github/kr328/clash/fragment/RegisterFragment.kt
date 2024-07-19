package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.adapter.MailAddressAdapter
import com.github.kr328.clash.design.databinding.FragRegisterAccountBinding
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel

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
            //调注册用户的API
            RequestHandler.request({
                val mailAddress = binding.editEmail.text.toString() + mailSuffix
                UserAccountApi.registerAccount(mailAddress, binding.editPassword.text.toString())
            }, {
                Log.d("init guest config data:$it")
                viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
            }, { code, msg ->
                context?.toast(msg)
                Log.e("initData fail: $msg")
            })
        }
        binding.titleBar.titleBarGoback.onClickNew {
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_LOGIN
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = RegisterFragment()
    }
}