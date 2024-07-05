package app.hw.network.api

import app.hw.network.RetrofitManager
import app.hw.network.model.AppConfig
import app.hw.network.model.CheckStat
import app.hw.network.model.LoginResp
import app.hw.network.model.ProductSubsInfo
import app.hw.network.model.UserInfo

/**
 * @Time : created on 2024/4/23 14:10
 * @Description :用户账户信息相关的服务端接口方法
 */
object UserAccountApi {
    private val service: UserAccountService by lazy {
        RetrofitManager.createApiService(
            UserAccountService::class.java
        )
    }

    suspend fun appConfig(): ResponseData<AppConfig> {
        return service.getAppConfig()
    }

    suspend fun checkLogin(): ResponseData<CheckStat> {
        return service.authCheck()
    }

    suspend fun login(mail: String, pwd: String): ResponseData<LoginResp> {
        val reqBody = RequestParam.Builder().apply {
            put("email", mail)
            put("password", pwd)
        }.build().requestBody
        return service.authLogin(reqBody)
    }

    suspend fun sendEmailVerifyCode(mail: String): ResponseData<Boolean> {
        val reqBody = RequestParam.Builder().apply {
            put("email", mail)
        }.build().requestBody
        return service.sendEMC(reqBody)
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
        mailCode: String,
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
        return service.authRegister(reqBody)
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
        return service.authForget(reqBody)
    }

    suspend fun logout(): ResponseData<Boolean> {
        return service.authLogout()
    }

    suspend fun userAccountInfo(): ResponseData<UserInfo> {
        return service.userInfo()
    }

    suspend fun getSubscribeInfo(): ResponseData<ProductSubsInfo> {
        return service.getSubscribe()
    }

    suspend fun resetSubsLink(): ResponseData<String> {
        return service.resetSubsLink()
    }

    suspend fun getTodos(): ResponseData<List<Long>> {
        return service.getStat()
    }

    suspend fun modifyPassword(oldPwd: String, newPwd: String): ResponseData<Boolean> {
        val reqBody = RequestParam.Builder().apply {
            put("old_password", oldPwd)
            put("new_password", newPwd)
        }.build().requestBody
        return service.changePwd(reqBody)
    }

    //TODO 佣金的单位和数量级
    suspend fun transferBonus(amount: Float): ResponseData<Boolean> {
        val reqBody = RequestParam.Builder().apply {
            put("transfer_amount", amount)
        }.build().requestBody
        return service.transferBonus(reqBody)
    }
}