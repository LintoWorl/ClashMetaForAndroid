package app.hw.network.api

import retrofit2.http.GET

internal interface OthersService {

    @GET("/user/invite/fetch")
    suspend fun fetchInviteCode(): ResponseData<String>

    @GET("/user/invite/save")
    suspend fun genInviteCode(): ResponseData<String>

    @GET("/user/invite/details")
    suspend fun inviteDetail(): ResponseData<String>

    @GET("/user/notice/fetch")
    suspend fun fetchNotice(): ResponseData<String>
}