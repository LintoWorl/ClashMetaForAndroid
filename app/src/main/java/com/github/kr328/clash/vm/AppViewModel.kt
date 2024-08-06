package com.github.kr328.clash.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class AppViewModel(app: Application) : AndroidViewModel(app) {
    val selectProxyName: MutableLiveData<String> by lazy { MutableLiveData<String>() }
}