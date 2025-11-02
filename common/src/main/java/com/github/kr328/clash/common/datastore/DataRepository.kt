package com.github.kr328.clash.common.datastore

import android.content.Context
import com.github.kr328.clash.common.datastore.DatastoreKeys.DM_APP_PREF
import com.github.kr328.clash.common.datastore.DatastoreKeys.DM_USER_PREF
import com.github.kr328.clash.common.datastore.DatastoreKeys.PREF_CURR_USERID
import com.github.kr328.clash.common.datastore.DatastoreKeys.PREF_USER_TOKEN
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * @Author : Pony
 * @Time : created on 2024/4/24 11:41
 * @Description :应用内的持久化数据仓库
 */
object DataRepository {

    private val appDatastore by lazy { DatastoreManager(DM_APP_PREF) }
    private lateinit var userDatastore: DatastoreManager

    fun initAppDataRepo(context: Context) {
        appDatastore.init(context)
    }

    /**
     * 用户登录后再调用
     */
    fun initUserDataRepo(context: Context) {
        runBlocking {
            //先取出当前登录的用户Id，构建与用户关联的DataStore对象
            val currUserId = appDatastore.getString(PREF_CURR_USERID).first()
            userDatastore = DatastoreManager(DM_USER_PREF + currUserId)
            userDatastore.init(context)
        }
    }

    fun clearData() {
        userDatastore.clearAll()
        appDatastore.putString(PREF_CURR_USERID, "")
        appDatastore.putString(PREF_USER_TOKEN, "")
    }

    fun globalDS(): DatastoreManager {
        return appDatastore
    }

    fun userDS(): DatastoreManager {
        return userDatastore
    }
}