package com.github.kr328.clash.store

import android.content.Context
import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.asStoreProvider

class AppStore(context: Context) {
    private val store = Store(
        context
            .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .asStoreProvider()
    )

    var updatedAt: Long by store.long(key = "updated_at", defaultValue = -1)
    var enteredHome: Boolean by store.boolean("user_entered_home", false)
    var hasLoginApp: Boolean by store.boolean("user_has_login", false)
    var userToken: String by store.string("user_lgn_token", "")
    var authData: String by store.string("user_auth_data", "")
    var tosAddress: String by store.string("user_tos_url", "https://bingoboot.com/user-agreement.html")
    var ppAddress: String by store.string("user_pripol_url", "https://bingoboot.com/privacy.html")
    var appWebsite: String by store.string("app_official_web", "https://bingoboot.com")

    companion object {
        private const val FILE_NAME = "app"
    }
}