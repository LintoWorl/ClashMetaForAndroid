package com.github.kr328.clash.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.hw.network.api.PaymentApi
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import app.hw.network.model.AppConfig
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class MainViewModel : ViewModel() {
    val fragIndex: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    companion object {
        const val IDX_FRAG_REPWD = -2
        const val IDX_FRAG_REGST = -1
        const val IDX_FRAG_LOGIN = 0
        const val IDX_FRAG_HOME = 1
        const val IDX_FRAG_SUBS = 2
        const val IDX_FRAG_USER = 3
    }

    val appConfig: MutableLiveData<AppConfig> by lazy { MutableLiveData<AppConfig>() }

    fun initConfigs(scope: CoroutineScope) {
        scope.launch {
            while (isActive) {
                select {
                    Global.commEvents.onReceive {
                        Logger.d("onReceive:$it, start request appConfig")
                        RequestHandler.request({
                            UserAccountApi.appConfig()
                        }, { config ->
                            Logger.d("got guest config data.")
                            appConfig.value = config
                        }, { _, msg ->
                            Logger.e("initData fail: $msg")
                        })
                    }
                }
            }
        }
    }

    fun fetchSubsPlan(isGuest: Boolean) {
        RequestHandler.request({
            PaymentApi.getSubsPlan(isGuest)
        },{

        },{code, msg ->

        })
    }
}