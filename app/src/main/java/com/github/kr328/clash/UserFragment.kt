package com.github.kr328.clash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.kr328.clash.design.databinding.FragUserCenterBinding

class UserFragment : Fragment() {

    private lateinit var binding: FragUserCenterBinding

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