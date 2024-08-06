package com.github.kr328.clash.vm

import com.github.kr328.clash.common.Global


object ViewModelManager {
    val appVM: AppViewModel by lazy { AppViewModel(Global.application) }
}