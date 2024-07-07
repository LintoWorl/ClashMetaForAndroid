package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.kr328.clash.design.databinding.FragRegisterAccountBinding
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.vm.MainViewModel

class RegisterFragment : Fragment() {
    private lateinit var binding: FragRegisterAccountBinding
    private val viewModel by activityViewModels<MainViewModel>()

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
    }

    private fun initView() {
        binding.titleBar.titleBarText.text = "注册"
        binding.btnRegister.onClickNew {
            //调注册用户的API
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