package com.github.kr328.clash.common.log

import android.content.Context
import android.widget.Toast

fun Context.toast(message: Int): Toast =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).apply { show() }

fun Context.toast(message: CharSequence): Toast =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).apply { show() }