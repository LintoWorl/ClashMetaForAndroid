package com.github.kr328.clash.store

import android.content.Context
import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.asStoreProvider

class TipsStore(context: Context) {
    private val store = Store(
        context
            .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .asStoreProvider()
    )

    var primaryVersion: Int by store.int(
        key = "primary_version",
        defaultValue = -1,
    )
    var updateProfTime: Long by store.long(key ="last_update_prof", -1)

    companion object {

        private const val FILE_NAME = "tips"
    }
}