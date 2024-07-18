package com.github.kr328.clash.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import app.hw.network.model.AppConfig
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Log
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
                        Log.d("onReceive:$it, start request appConfig")
                        RequestHandler.request({
                            UserAccountApi.appConfig()
                        }, { config ->
                            Log.d("got guest config data.")
                            appConfig.value = config
                        }, { _, msg ->
                            Log.e("initData fail: $msg")
                        })
                    }
                }
            }
        }
    }
}