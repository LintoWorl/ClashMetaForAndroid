package com.github.kr328.clash.design.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatCheckBox


class CenteredCheckBox : AppCompatCheckBox {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrS: AttributeSet) : super(context, attrS)
    constructor(context: Context, attrS: AttributeSet, defStyAttr: Int)
            : super(context, attrS, defStyAttr)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val drawables = getCompoundDrawables()
        val drawable = drawables[0] ?: drawables[2]
        val gravity = gravity
        var left = 0
        drawable?.apply {
            if (gravity == Gravity.CENTER) {
                left =
                    (width - intrinsicWidth - paint.measureText(getText().toString())).toInt() / 2
            }
            setBounds(left, 0, left + intrinsicWidth, intrinsicHeight)
        }
    }
}