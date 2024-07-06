package com.github.kr328.clash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.kr328.clash.design.databinding.FragLoginAccountBinding
import com.github.kr328.clash.design.util.onClickNew

class LoginFragment : Fragment() {
    private lateinit var binding: FragLoginAccountBinding

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

        }
        binding.btnEnterTourist.onClickNew {

        }
        binding.btnEnterRegister.onClickNew {

        }
        binding.tvForgetPwd.onClickNew {

        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}