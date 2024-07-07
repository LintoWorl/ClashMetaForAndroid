package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.kr328.clash.design.databinding.FragLoginAccountBinding
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.vm.MainViewModel

class LoginFragment : Fragment() {
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
    }

    private fun initView() {
        binding.btnLoginAccount.onClickNew {
            //用账号登录
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_HOME
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

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}