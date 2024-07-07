package com.github.kr328.clash.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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
}