package app.hw.network.api

import app.hw.network.model.InviteCodeResp
import app.hw.network.model.InviteDetail
import app.hw.network.model.NoticeBean
import retrofit2.http.GET
import retrofit2.http.Query

internal interface OthersService {

    @GET("/user/invite/fetch")
    suspend fun fetchInviteCode(): ResponseData<InviteCodeResp>

    @GET("/user/invite/save")
    suspend fun genInviteCode(): ResponseData<Boolean>

    @GET("/user/invite/details")
    suspend fun inviteDetail(): ResponseData<InviteDetail>

    @GET("user/notice/fetch")
    suspend fun fetchNotice(): ResponseData<List<NoticeBean>>

    @GET("client/app/getVersion")
    suspend fun checkAppVer(@Query("token") token: String): ResponseData<String>
}