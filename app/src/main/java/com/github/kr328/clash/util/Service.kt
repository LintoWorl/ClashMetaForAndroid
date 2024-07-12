package com.github.kr328.clash.util

import android.content.Context
import android.content.ServiceConnection

fun Context.unbindServiceSilent(connection: ServiceConnection) {
    try {
        unbindService(connection)
    } catch (e: Exception) {
        // ignore
    }
}

const val subsUrl = "https://s.jiasu01.vip/bd/api/v1/client/subscribe?token=07d0d5175bf41ae8c335128597e20e9e&flag=clash"