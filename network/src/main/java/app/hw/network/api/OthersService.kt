package app.hw.network.api

import app.hw.network.model.InviteCodeResp
import app.hw.network.model.InviteDetail
import app.hw.network.model.NoticeBean
import retrofit2.http.GET

internal interface OthersService {

    @GET("/user/invite/fetch")
    suspend fun fetchInviteCode(): ResponseData<InviteCodeResp>

    @GET("/user/invite/save")
    suspend fun genInviteCode(): ResponseData<Boolean>

    @GET("/user/invite/details")
    suspend fun inviteDetail(): ResponseData<InviteDetail>

    @GET("user/notice/fetch")
    suspend fun fetchNotice(): ResponseData<List<NoticeBean>>
}