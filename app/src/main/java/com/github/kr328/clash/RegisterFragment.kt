package com.github.kr328.clash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.kr328.clash.design.databinding.FragRegisterAccountBinding
import com.github.kr328.clash.design.util.onClickNew

class RegisterFragment : Fragment() {
    private lateinit var binding: FragRegisterAccountBinding

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
        binding.btnRegister.onClickNew {

        }
        binding.titleBar.titleBarGoback.onClickNew {

        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = RegisterFragment()
    }
}