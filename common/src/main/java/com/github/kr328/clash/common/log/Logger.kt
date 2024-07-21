package com.github.kr328.clash.common.log

import android.util.Log

object Logger {
    private const val TAG = "FHL_Common"
    const val TAG_HTTP = "Http_Resp"
    const val TAG_EXP = "Http_Exp"

    fun i(message: String, throwable: Throwable? = null) =
        Log.i(TAG, message, throwable)

    fun w(message: String, throwable: Throwable? = null) =
        Log.w(TAG, message, throwable)

    fun e(message: String, throwable: Throwable? = null) =
        Log.e(TAG, message, throwable)

    fun d(message: String, throwable: Throwable? = null) =
        Log.d(TAG, message, throwable)

    fun v(message: String, throwable: Throwable? = null) =
        Log.v(TAG, message, throwable)

    fun f(message: String, throwable: Throwable) =
        Log.wtf(message, throwable)
}
