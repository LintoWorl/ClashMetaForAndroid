package com.github.kr328.clash.common.datastore

/**
 * @Author : Pony
 * @Time : created on 2024/4/24 11:29
 * @Description :定义应用内持久化数据的Key
 */
object DatastoreKeys {
    //应用全局的DataStore文件名
    const val DM_APP_PREF = "prefs_dm"

    //与用户相关的DataStore文件名前缀
    const val DM_USER_PREF = "prefs_dm_user_"

    //当前登录的用户Id
    const val PREF_CURR_USERID = "pref_curr_userid"

    //用户登录后的token信息
    const val PREF_USER_TOKEN = "pref_user_token"
}