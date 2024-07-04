package app.hw.network.api

import app.hw.network.model.AppConfig
import app.hw.network.model.CheckStat
import app.hw.network.model.LoginResp
import app.hw.network.model.UserInfo
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * @Time : created on 2024/4/22 10:00
 * @Description :用户账户信息相关的服务端Api
 */
internal interface UserAccountService {

    @GET("/passport/comm/config")
    suspend fun getAppConfig(): ResponseData<AppConfig>

    /**
     * 校验登录
     */
    @GET("/passport/auth/check")
    suspend fun authCheck(): ResponseData<CheckStat>

    /**
     * 账号登录
     */
    @POST("/passport/auth/login")
    suspend fun authLogin(@Body requestBody: RequestBody): ResponseData<LoginResp>

    /**
     * 发送邮箱验证码
     */
    @POST("/passport/comm/sendEmailVerify")
    suspend fun sendEMC(@Body requestBody: RequestBody): ResponseData<Boolean>

    /**
     * 注册账号
     */
    @POST("/passport/auth/register")
    suspend fun authRegister(@Body requestBody: RequestBody): ResponseData<LoginResp>

    /**
     * 重置密码
     */
    @POST("/passport/auth/forget")
    suspend fun authForget(@Body requestBody: RequestBody): ResponseData<Boolean>

    /**
     * 退出登录
     */
    @GET("/user/logout")
    suspend fun authLogout(): ResponseData<Boolean>

    /**
     * 用户账号信息
     */
    @GET("/user/info")
    suspend fun userInfo(): ResponseData<UserInfo>

    /**
     * 获取订阅信息
     */
    @GET("/user/getSubscribe")
    suspend fun getSubscribe(): ResponseData<String>

    /**
     * 重置订阅链接
     */
    @GET("/user/resetSecurity")
    suspend fun resetSubsLink(): ResponseData<String>

    /**
     * 获取待办事项
     */
    @GET("/user/getStat")
    suspend fun getStat(): ResponseData<String>

    /**
     * 修改秘密
     */
    @POST("/user/changePassword")
    suspend fun changePwd(): ResponseData<String>

    /**
     * 通知状态
     */
    @POST("/user/update")
    suspend fun updateStat(): ResponseData<String>

    @POST("/user/transfer")
    suspend fun transferBonus(): ResponseData<String>
}