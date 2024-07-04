package app.hw.network.api

import app.hw.network.RetrofitManager

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

    suspend fun loginDevice(): ResponseData<String> {
        return service.loginDevice()
    }

    suspend fun loginGoogle(): ResponseData<String> {
        return service.loginGoogle()
    }

    suspend fun loginFacebook(): ResponseData<String> {
        return service.loginFacebook()
    }

    suspend fun submitUserId(): ResponseData<String> {
        return service.submitUserId()
    }

    suspend fun deleteUserAccount(): ResponseData<String> {
        return service.deleteUserAccount()
    }

    suspend fun logoutUserAccount(): ResponseData<String> {
        return service.logoutUserAccount()
    }

    suspend fun queryUserInfo(): ResponseData<String> {
        return service.queryUserInfo()
    }

    suspend fun updateUserInfo(): ResponseData<String> {
        return service.updateUserInfo()
    }

    suspend fun queryHobbies(): ResponseData<String> {
        return service.queryHobbies()
    }

    suspend fun sendVerifyCode(): ResponseData<String> {
        return service.sendVerifyCode()
    }

    suspend fun bindEmail(): ResponseData<String> {
        return service.bindEmail()
    }

    suspend fun unbindEmail(): ResponseData<String> {
        return service.unbindEmail()
    }
}