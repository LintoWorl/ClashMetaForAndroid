package com.github.kr328.clash.design.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import java.util.*

fun View.requestTextInput() {
    post {
        requestFocus()

        postDelayed({
            context.getSystemService<InputMethodManager>()
                ?.showSoftInput(this, 0)
        }, 300)
    }
}

fun View.hideKeyboard(): Boolean {
    val mInputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return mInputManager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.show(): View {
    this.visibility = View.VISIBLE
    return this
}

fun View.hide(): View {
    this.visibility = View.GONE
    return this
}

fun View.invisible(): View {
    this.visibility = View.INVISIBLE
    return this
}

abstract class NoDoubleClickListener(val delay: Int) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val currentTime = Calendar.getInstance().timeInMillis
        if (currentTime - lastClickTime >= delay) {
            onNoDoubleClick(v)
        }
        lastClickTime = currentTime
    }

    abstract fun onNoDoubleClick(v: View)

    companion object {
        const val MIN_CLICK_DELAY_TIME = 500
    }
}

fun View.onClickNew(delay: Int = NoDoubleClickListener.MIN_CLICK_DELAY_TIME, l: (v: View) -> Unit) {
    setOnClickListener(object : NoDoubleClickListener(delay) {
        override fun onNoDoubleClick(v: View) {
            l(v)
        }
    })
}
