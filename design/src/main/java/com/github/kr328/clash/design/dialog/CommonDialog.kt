package com.github.kr328.clash.design.dialog

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.DialogCommomBaseBinding
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.design.util.show
import io.noties.markwon.Markwon

open class CommonDialog() : DialogFragment() {
    private lateinit var binding: DialogCommomBaseBinding
    private var cancelable: Boolean = false
    private var dismissByCancel: Boolean = false
    private var singleBtn: Boolean = false
    private var title: String? = ""
    private var message: String = ""
    private var leftBtnTxt: String? = ""
    private var rightBtnTxt: String? = ""
    private var dlgListener: OnClickListener? = null

    companion object {
        inline fun show(fragmentManager: FragmentManager, func: Builder.() -> Unit) {
            val dialog = Builder().apply(func).build()
            dialog.show(fragmentManager, "CommonDialog")
        }
    }

    constructor(builder: Builder) : this() {
        this.title = builder.title
        this.message = builder.content
        this.leftBtnTxt = builder.leftButton
        this.rightBtnTxt = builder.rightButton
        this.dlgListener = builder.listener
        this.cancelable = builder.autoCancel
        this.singleBtn = builder.onlyConfirm
    }

    class Builder {
        var autoCancel: Boolean = false
        var onlyConfirm: Boolean = false
        var title: String? = ""
        var content: String = ""
        var leftButton: String? = ""
        var rightButton: String? = ""
        var listener: OnClickListener? = null

        fun build(): CommonDialog {
            return CommonDialog(this)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(cancelable)
        dialog?.setCanceledOnTouchOutside(cancelable)
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
        binding = DialogCommomBaseBinding.inflate(inflater, container, false)
        initViews()
        initEvents()
        return binding.root
    }

    private fun initViews() {
        val markwon = Markwon.create(Global.application)
        binding.dialogTvTitle.text =
            if (title == null || title?.isEmpty() == true) {
                getString(R.string.launch_name)
            } else title
        markwon.setMarkdown(binding.dialogTvContent, message)

        if (singleBtn) binding.dialogBtnNegative.hide()
        else binding.dialogBtnNegative.show()
        binding.dialogBtnNegative.text =
            if (leftBtnTxt == null || leftBtnTxt?.isEmpty() == true) {
                getString(R.string.cancel)
            } else leftBtnTxt

        binding.dialogBtnPositive.text =
            if (rightBtnTxt == null || rightBtnTxt?.isEmpty() == true) {
                getString(R.string.ok)
            } else rightBtnTxt

        dismissByCancel = true
    }

    private fun initEvents() {
        binding.dialogBtnPositive.onClickNew {
            dlgListener?.onPositiveClick(this, "")
            dismissByCancel = true
            dismiss()
        }
        binding.dialogBtnNegative.onClickNew {
            dlgListener?.onNegativeClick(this)
            dismiss()
        }
    }

    protected fun onCreateContentView(container: ViewGroup): View? {
        return null
    }

    /**
     * 复写此方法， 并可在此方法中设置，回调监听确定按钮所需的数据
     *
     * @return
     */
    protected fun getPositiveData(): Bundle? {
        return null
    }

    /**
     * 复写此方法， 并可在此方法中设置，回调监听取消按钮所需的数据
     *
     * @return
     */
    protected fun getNegativeData(): Bundle? {
        return null
    }

    /**
     * 集成的子类假如想在内部处理 Positive 点击监听， 可复写此方法。 返回 true 则可拦截，不会走外部设置的点击监听
     *
     * @return true 拦截监听， false 不拦截
     */
    protected fun onPositiveClick(): Boolean {
        return false
    }

    /**
     * 集成的子类假如想在内部处理 Negative 点击监听， 可复写此方法。 返回 true 则可拦截，不会走外部设置的点击监听
     *
     * @return
     */
    protected fun onNegativeClick(): Boolean {
        return false
    }

    protected fun onDialogDismiss() {}

    interface OnClickListener {
        fun onPositiveClick(dialog: CommonDialog, clue: String)
        fun onNegativeClick(dialog: CommonDialog)
    }
}