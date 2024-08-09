package com.github.kr328.clash.design.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.github.kr328.clash.design.R;

import androidx.annotation.Nullable;


public class DrawableTextView extends androidx.appcompat.widget.AppCompatTextView {
    private int drawableWidth, drawableHeight;

    public DrawableTextView(Context context) {
        this(context, null);
    }

    public DrawableTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);
        int count = typedArray.getIndexCount();
        Drawable drawableLeft = null;
        Drawable drawableTop = null;
        Drawable drawableRight = null;
        Drawable drawableBottom = null;
        for (int i = 0; i < count; i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.DrawableTextView_drawableEnd) {
                drawableRight = typedArray.getDrawable(attr);
            } else if (attr == R.styleable.DrawableTextView_drawableStart) {
                drawableLeft = typedArray.getDrawable(attr);
            } else if (attr == R.styleable.DrawableTextView_drawableTop) {
                drawableTop = typedArray.getDrawable(attr);
            } else if (attr == R.styleable.DrawableTextView_drawableBottom) {
                drawableBottom = typedArray.getDrawable(attr);
            } else if (attr == R.styleable.DrawableTextView_drawableWidth) {
                drawableWidth = typedArray.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.DrawableTextView_drawableHeight) {
                drawableHeight = typedArray.getDimensionPixelSize(attr, 0);
            }
        }
        typedArray.recycle();
        if (null != drawableLeft) {
            drawableLeft.setBounds(0, 0, drawableWidth, drawableHeight);
        }
        if (null != drawableRight) {
            drawableRight.setBounds(0, 0, drawableWidth, drawableHeight);
        }
        if (null != drawableTop) {
            drawableTop.setBounds(0, 0, drawableWidth, drawableHeight);
        }
        if (null != drawableBottom) {
            drawableBottom.setBounds(0, 0, drawableWidth, drawableHeight);
        }
        setCompoundDrawables(drawableLeft, drawableTop, drawableRight, drawableBottom);
    }

}