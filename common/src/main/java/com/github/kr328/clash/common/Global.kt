package com.github.kr328.clash.common

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel

object Global : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    val application: Application
        get() = application_

    private lateinit var application_: Application
    val commEvents = Channel<String>(Channel.UNLIMITED)
    fun init(application: Application) {
        this.application_ = application
    }

    fun destroy() {
        cancel()
    }
}