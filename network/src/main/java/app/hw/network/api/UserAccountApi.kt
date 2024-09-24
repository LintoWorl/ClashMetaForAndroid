package app.hw.network.api

import app.hw.network.RetrofitManager
import app.hw.network.model.AppConfig
import app.hw.network.model.CheckStat
import app.hw.network.model.LoginResp
import app.hw.network.model.ProductSubsInfo
import app.hw.network.model.TrafficBean
import app.hw.network.model.UserInfo
import app.hw.network.util.NetworkUtil
import com.github.kr328.clash.common.Global

/**
 * @Time : created on 2024/4/23 14:10
 * @Description :用户账户信息相关的服务端接口方法
 */
object UserAccountApi {
    private val service: UserAccountService by lazy {
        RetrofitManager.createService(
            UserAccountService::class.java
        )
    }
    private val service2: UserAccountService by lazy {
        RetrofitManager.createApiService(
            UserAccountService::class.java
        )
    }

    suspend fun appConfig(): ResponseData<AppConfig> {
        return if (NetworkUtil.isVpnRunning(Global.application)) {
            service2.getAppConfig()
        } else {
            service.getAppConfig()
        }
    }

    suspend fun checkLogin(): ResponseData<CheckStat> {
        return service.authCheck()
    }

    suspend fun login(mail: String, pwd: String): ResponseData<LoginResp> {
        val reqBody = RequestParam.Builder().apply {
            put("email", mail)
            put("password", pwd)
        }.build().requestBody
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.authLogin(reqBody)
        }
        return service.authLogin(reqBody)
    }

    suspend fun sendEmailVerifyCode(mail: String): ResponseData<Boolean> {
        val reqBody = RequestParam.Builder().apply {
            put("email", mail)
        }.build().requestBody
        return if (NetworkUtil.isVpnRunning(Global.application)) {
            service2.sendEMC(reqBody)
        } else {
            service.sendEMC(reqBody)
        }
    }

    /**
     * 注册账号，参数：
     *  {
     *   "email": "xxxx@xx.com",
     *   "password": "123456789",
     *   "email_code": 333333,
     *   "invite_code": "",
     *   "recaptcha_data": ""
     * }
     */
    suspend fun registerAccount(
        mail: String,
        pwd: String,
        mailCode: String = "",
        inviteCode: String = "",
        verifyCode: String = ""
    ): ResponseData<LoginResp> {
        val reqBody = RequestParam.Builder().apply {
            put("email", mail)
            put("password", pwd)
            put("email_code", mailCode)
            put("invite_code", inviteCode)
            put("recaptcha_data", verifyCode)
        }.build().requestBody
        return if (NetworkUtil.isVpnRunning(Global.application)) {
            service2.authRegister(reqBody)
        } else {
            service.authRegister(reqBody)
        }
    }

    suspend fun forgetAccount(
        mail: String,
        pwd: String,
        mailCode: String
    ): ResponseData<Boolean> {
        val reqBody = RequestParam.Builder().apply {
            put("email", mail)
            put("password", pwd)
            put("email_code", mailCode)
        }.build().requestBody
        return if (NetworkUtil.isVpnRunning(Global.application)) {
            service2.authForget(reqBody)
        } else {
            service.authForget(reqBody)
        }
    }

    suspend fun logout(): ResponseData<Boolean> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.authLogout()
        }
        return service.authLogout()
    }

    suspend fun userAccountInfo(): ResponseData<UserInfo> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.userInfo()
        }
        return service.userInfo()
    }

    suspend fun getSubscribeInfo(): ResponseData<ProductSubsInfo> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.getSubscribe()
        }
        return service.getSubscribe()
    }

    suspend fun resetSubsLink(): ResponseData<String> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.resetSubsLink()
        }
        return service.resetSubsLink()
    }

    suspend fun getTodos(): ResponseData<List<Long>> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.getStat()
        }
        return service.getStat()
    }

    suspend fun modifyPassword(oldPwd: String, newPwd: String): ResponseData<Boolean> {
        val reqBody = RequestParam.Builder().apply {
            put("old_password", oldPwd)
            put("new_password", newPwd)
        }.build().requestBody
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.changePwd(reqBody)
        }
        return service.changePwd(reqBody)
    }

    //TODO 佣金的单位和数量级
    suspend fun transferBonus(amount: Float): ResponseData<Boolean> {
        val reqBody = RequestParam.Builder().apply {
            put("transfer_amount", amount)
        }.build().requestBody
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.transferBonus(reqBody)
        }
        return service.transferBonus(reqBody)
    }

    suspend fun getTrafficLog(): ResponseData<List<TrafficBean>> {
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.trafficRecord()
        }
        return service.trafficRecord()
    }
}