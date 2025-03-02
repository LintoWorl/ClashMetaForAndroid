package app.hw.network.api

import app.hw.network.model.InviteCodeResp
import app.hw.network.model.InviteDetail
import app.hw.network.model.NoticeBean
import app.hw.network.model.VerInfo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface OthersService {

    @GET("/api/v1/user/invite/fetch")
    suspend fun fetchInviteCode(): ResponseData<InviteCodeResp>

    @GET("/api/v1/user/invite/save")
    suspend fun genInviteCode(): ResponseData<Boolean>

    @GET("/api/v1/user/invite/details")
    suspend fun inviteDetail(): ResponseData<InviteDetail>

    @GET("/api/v1/user/notice/fetch")
    suspend fun fetchMemberNotice(): ResponseData<List<NoticeBean>>
    @GET("fhl/notice/{XYZ}")
    suspend fun fetchNotice(@Path("XYZ") xyz: String): ResponseData<List<NoticeBean>>

    @GET("/api/v1/client/app/getVersion")
    suspend fun checkAppVer(@Query("token") token: String): ResponseData<VerInfo>
}