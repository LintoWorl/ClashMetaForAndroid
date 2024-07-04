package app.hw.network.api

import retrofit2.http.POST

/**
 * @Time : created on 2024/4/22 10:00
 * @Description :用户账户信息相关的服务端Api
 */
internal interface UserAccountService {

    /**
     * 设备激活，游客设备登录
     */
    @POST("/aisocial/lgin/visitor")
    suspend fun loginDevice(): ResponseData<String>

    /**
     * 通过Google登录
     */
    @POST("/aisocial/lgin/google")
    suspend fun loginGoogle(): ResponseData<String>

    /**
     * 通过Facebook登录
     */
    @POST("/aisocial/lgin/facebook")
    suspend fun loginFacebook(): ResponseData<String>

    /**
     * 将三方平台登录后生成的UserId插入本应用的平台账号体系
     */
    @POST("/social/userinfo/insertAccount")
    suspend fun submitUserId(): ResponseData<String>

    /**
     * 删除用户账号
     */
    @POST("/aisocial/lgin/closeaccount")
    suspend fun deleteUserAccount(): ResponseData<String>

    /**
     * 退出登录用户账号
     */
    @POST("/aisocial/lgin/loginout")
    suspend fun logoutUserAccount(): ResponseData<String>

    /**
     * 获取用户基本信息
     */
    @POST("/social/userinfo/baseinfo")
    suspend fun queryUserInfo(): ResponseData<String>

    /**
     * 更新用户基本信息
     */
    @POST("/social/userinfo/update")
    suspend fun updateUserInfo(): ResponseData<String>

    /**
     * 获取兴趣爱好列表
     */
    @POST("/social/userinfo/queryHobby")
    suspend fun queryHobbies(): ResponseData<String>

    /**
     * 邮箱发送验证码
     */
    @POST("/social/userinfo/send/verifyCode")
    suspend fun sendVerifyCode(): ResponseData<String>

    /**
     * 绑定邮箱
     */
    @POST("/social/userinfo/bindEmail")
    suspend fun bindEmail(): ResponseData<String>

    /**
     * 解除邮箱绑定
     */
    @POST("/social/userinfo/unbindEmail")
    suspend fun unbindEmail(): ResponseData<String>
}