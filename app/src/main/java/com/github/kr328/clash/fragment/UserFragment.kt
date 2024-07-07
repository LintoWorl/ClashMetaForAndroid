package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.kr328.clash.design.databinding.FragUserCenterBinding
import com.github.kr328.clash.vm.MainViewModel

class UserFragment : Fragment() {

    private lateinit var binding: FragUserCenterBinding
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragUserCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

    }

    fun updateView(content: String) {
        binding.tvDescription.text = content
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserFragment()
    }
}