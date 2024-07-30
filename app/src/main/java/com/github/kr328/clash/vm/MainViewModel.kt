package com.github.kr328.clash.vm

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.hw.network.api.OthersApi
import app.hw.network.api.PaymentApi
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import app.hw.network.model.AppConfig
import app.hw.network.model.LoginResp
import app.hw.network.model.NoticeBean
import app.hw.network.model.ProductSubsInfo
import app.hw.network.model.SubsProductBean
import app.hw.network.model.UserInfo
import com.github.kr328.clash.MainV2Activity
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.dialog.showModalProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        const val IDX_FRAG_XPWD = 4
    }

    val appConfig: MutableLiveData<AppConfig> by lazy { MutableLiveData<AppConfig>() }
    val subsInfo: MutableLiveData<ProductSubsInfo> by lazy { MutableLiveData<ProductSubsInfo>() }
    val userInfo: MutableLiveData<UserInfo> by lazy { MutableLiveData<UserInfo>() }
    val subsPlanList: MutableLiveData<List<SubsProductBean>> by lazy { MutableLiveData<List<SubsProductBean>>() }
    val lgnStatChngd: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val noticeMsgList: MutableLiveData<List<NoticeBean>> by lazy { MutableLiveData<List<NoticeBean>>() }

    fun checkLoginStat() {
        RequestHandler.request({
            UserAccountApi.checkLogin()
        }, {}, { code, msg ->
            Logger.e("checkLoginStat fail:$msg")
        })
    }

    fun initConfigs(scope: Activity) {
        CoroutineScope(Dispatchers.Main).launch {
            scope.showModalProgressBar {
                configure {
                    isIndeterminate = true
                    text = "正在获取配置信息，请稍候..."
                }
                RequestHandler.request({
                    UserAccountApi.appConfig()
                }, { config ->
                    Logger.d("got guest config data.")
                    appConfig.value = config
                    onResult()
                }, { _, msg ->
                    Logger.e("initData fail: $msg")
                    onResult()
                })
            }
//            while (isActive) {
//                select {
//                    Global.commEvents.onReceive {
//                        Logger.d("onReceive:$it, start request appConfig")
//
//                    }
//                }
//            }
        }
    }

    fun loginApp(
        mailAddr: String,
        pwd: String,
        onSucc: (LoginResp) -> Unit,
        onFail: (String) -> Unit
    ) {
        RequestHandler.request({
            UserAccountApi.login(mailAddr, pwd)
        }, { lgn ->
            Logger.d("login account:$lgn")
            onSucc(lgn)
            fragIndex.value = IDX_FRAG_HOME
            lgnStatChngd.postValue(true)
        }, { _, msg ->
            onFail(msg)
        })
    }

    suspend fun fetchUserAccountInfo(context: Context) {
        context.showModalProgressBar {
            configure {
                isIndeterminate = true
                text = "更新数据，请稍候..."
            }
            RequestHandler.request({
                UserAccountApi.userAccountInfo()
            }, {
                Logger.d("got userInfo:${it.email}")
                userInfo.value = it
                onResult()
            }, { code, msg ->
                context.toast(msg)
                onResult()
            })
        }
    }

    fun fetchSubscribeInfo() {
        RequestHandler.request({
            UserAccountApi.getSubscribeInfo()
        }, {
            subsInfo.value = it
        }, { code, msg ->
            Logger.e("fetchSubscribeInfo fail:$msg")
        })
    }

    fun fetchNoticeInfo() {
        RequestHandler.request({
            OthersApi.getNoticeMsg()
        }, {
            noticeMsgList.value = it
        }, { code, msg ->
            Logger.e("fetchNoticeInfo fail:$msg")
        })
    }

    fun fetchSubsPlan(isGuest: Boolean, onFinish: () -> Unit = {}) {
        RequestHandler.request({
            PaymentApi.getSubsPlan(isGuest)
        }, {
            subsPlanList.value = it
            onFinish()
        }, { code, msg ->
            Logger.d("fetchSubsPlan fail:$msg")
            onFinish()
        })
    }

    suspend fun modifyUserPwd(context: Context, oldPwd: String, newPwd: String) {
        context.showModalProgressBar {
            configure {
                isIndeterminate = true
                text = "正在处理..."
            }
            RequestHandler.request({
                UserAccountApi.modifyPassword(oldPwd, newPwd)
            }, {
                onResult()
                if (it) {
                    context.toast("重置密码成功")
                    fragIndex.value = IDX_FRAG_USER
                } else {
                    context.toast("重置密码失败，请稍后重试")
                }
            }, { _, msg ->
                context.toast(msg)
                onResult()
            })
        }
    }
}