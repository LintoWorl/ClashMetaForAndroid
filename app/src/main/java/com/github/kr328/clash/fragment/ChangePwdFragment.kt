package com.github.kr328.clash.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.kr328.clash.common.compat.checkEmpty
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.databinding.FragChangePasswordBinding
import com.github.kr328.clash.design.util.hideKeyboard
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChangePwdFragment : Fragment() {
    private lateinit var binding: FragChangePasswordBinding
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.titleBar.titleBarText.text = "修改密码"
        binding.root.onClickNew { it.hideKeyboard() }
        binding.btnResetPwd.onClickNew {
            if (binding.editOldPwd.checkEmpty("请输入原密码")
                || binding.editNewPwd.checkEmpty("请输入新密码")
                || binding.confirmNewPwd.checkEmpty("请确认新密码")
            ) {
                return@onClickNew
            }

            it.hideKeyboard()
            //调重置用户密码的API
            val oldPwd = binding.editOldPwd.text.toString()
            val newPwd = binding.editNewPwd.text.toString()
            if (oldPwd != newPwd) {
                context?.toast("请确认两次输入的密码内容相同")
                return@onClickNew
            }
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.modifyUserPwd(requireContext(), oldPwd, newPwd)
            }
        }
        binding.titleBar.titleBarGoback.onClickNew {
            it.hideKeyboard()
            viewModel.fragIndex.value = MainViewModel.IDX_FRAG_USER
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
        fun newInstance() = ChangePwdFragment()
    }
}