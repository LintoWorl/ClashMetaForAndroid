package com.github.kr328.clash.vm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.hw.network.api.OthersApi
import app.hw.network.api.PaymentApi
import app.hw.network.api.UserAccountApi
import app.hw.network.handler.RequestHandler
import app.hw.network.model.AppConfig
import app.hw.network.model.CouponBean
import app.hw.network.model.LoginResp
import app.hw.network.model.NoticeBean
import app.hw.network.model.PaymentBean
import app.hw.network.model.ProductSubsInfo
import app.hw.network.model.SubsProductBean
import app.hw.network.model.UserInfo
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.common.log.toast
import com.github.kr328.clash.design.dialog.showModalProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    var userHasLogin: Boolean = false
    var prevFragIdx: Int = IDX_FRAG_LOGIN
    val appConfig: MutableLiveData<AppConfig> by lazy { MutableLiveData<AppConfig>() }
    val subsInfo: MutableLiveData<ProductSubsInfo> by lazy { MutableLiveData<ProductSubsInfo>() }
    val userInfo: MutableLiveData<UserInfo> by lazy { MutableLiveData<UserInfo>() }
    val subsPlanList: MutableLiveData<List<SubsProductBean>> by lazy { MutableLiveData<List<SubsProductBean>>() }
    val lgnState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val noticeMsgList: MutableLiveData<List<NoticeBean>> by lazy { MutableLiveData<List<NoticeBean>>() }
    var subsOrderId: String = ""//MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val paymentMethodList: MutableLiveData<List<PaymentBean>> by lazy { MutableLiveData<List<PaymentBean>>() }
    val couponBean: MutableLiveData<CouponBean> by lazy { MutableLiveData<CouponBean>() }

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
            lgnState.postValue(true)
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
            updateUserInfo { onResult() }
        }
    }

    fun updateUserInfo(finished: () -> Unit = {}) {
        RequestHandler.request({
            UserAccountApi.userAccountInfo()
        }, {
            Logger.d("got userInfo:${it.email}, lastLgn:${it.last_login_at}")
            userInfo.value = it
            finished()
        }, { code, msg ->
            Global.application.toast(msg)
            finished()
        })
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

    fun validateCoupon(couponCode: String, plan: SubsProductBean, onSucc: (CouponBean) -> Unit) {
        RequestHandler.request({
            PaymentApi.checkCoupon(couponCode, plan.id)
        }, {
            //couponBean.value = it
            onSucc(it)
        }, { code, msg ->
            Global.application.toast(msg)
        })
    }

    fun getPaymentMethod() {
        RequestHandler.request({
            PaymentApi.getPayMethod()
        }, {
            paymentMethodList.value = it
        }, { code, msg ->
            Global.application.toast(msg)
        })
    }

    fun commitSubsOrder(paymentBean: PaymentBean) {
        if (subsOrderId.isNotEmpty()) {
            RequestHandler.request({
                PaymentApi.payOrder(subsOrderId, paymentBean.id)
            }, {
                Logger.d("submit order:$it")
                if (it.isEmpty()) {
                    Global.application.toast("支付订单失败！请重试")
                    return@request
                }
                val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                actionIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                Global.application.startActivity(actionIntent)
            }, { code, msg -> Global.application.toast(msg) })
        }
    }

    fun cancelSubsOrder() {
        if (subsOrderId.isNotEmpty()) {
            RequestHandler.request({ PaymentApi.cancelOrder(subsOrderId) },
                {
                    Global.application.toast(if (it) "取消成功" else "取消失败了")
                }, { code, msg -> Global.application.toast(msg) })
        }
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