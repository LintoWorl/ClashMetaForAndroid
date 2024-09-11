package com.github.kr328.clash.design.dialog

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.DialogEditableCommBinding
import com.github.kr328.clash.design.util.onClickNew

class EditableDialog() : DialogFragment() {
    private lateinit var binding: DialogEditableCommBinding
    private var title: String? = ""
    private var leftBtnTxt: String? = ""
    private var rightBtnTxt: String? = ""
    private var dlgListener: OnClickListener? = null

    companion object {
        inline fun show(fragmentManager: FragmentManager, func: Builder.() -> Unit) {
            val dialog = Builder().apply(func).build()
            //val dialog = EditableDialog()
            dialog.show(fragmentManager, "EditableDialog")
        }
    }

    constructor(builder: Builder) : this() {
        this.title = builder.title
        this.leftBtnTxt = builder.leftButton
        this.rightBtnTxt = builder.rightButton
        this.dlgListener = builder.listener
    }

    class Builder {
        var title: String? = ""
        var leftButton: String? = ""
        var rightButton: String? = ""
        var listener: OnClickListener? = null

        fun build(): EditableDialog {
            return EditableDialog(this)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MatchWidthDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogEditableCommBinding.inflate(inflater, container, false)
        initEvents()
        return binding.root
    }

    private fun initEvents() {
        binding.dialogTvTitle.text =
            if (title == null || title?.isEmpty() == true) {
                getString(R.string.launch_name)
            } else title
        binding.dialogBtnNegative.text =
            if (leftBtnTxt == null || leftBtnTxt?.isEmpty() == true) {
                getString(R.string.cancel)
            } else leftBtnTxt
        binding.dialogBtnPositive.text =
            if (rightBtnTxt == null || rightBtnTxt?.isEmpty() == true) {
                getString(R.string.ok)
            } else rightBtnTxt

        binding.dialogBtnPositive.onClickNew {
            dlgListener ?: dismiss()
            dlgListener?.apply {
                onPositiveClick(this@EditableDialog, binding.dialogContent.editableText.toString())
            }
        }
        binding.dialogBtnNegative.onClickNew {
            dlgListener ?: dismiss()
            dlgListener?.apply { onNegativeClick(this@EditableDialog) }
        }
    }

    interface OnClickListener {
        fun onPositiveClick(dialog: EditableDialog, editContent: String)
        fun onNegativeClick(dialog: EditableDialog)
    }
}