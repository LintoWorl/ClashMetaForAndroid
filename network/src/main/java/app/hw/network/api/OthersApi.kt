package app.hw.network.api

import app.hw.network.RetrofitManager
import app.hw.network.model.InviteCodeResp
import app.hw.network.model.InviteDetail
import app.hw.network.model.NoticeBean


object OthersApi {

    private val service: OthersService by lazy {
        RetrofitManager.createApiService(OthersService::class.java)
    }

    suspend fun getInviteCodeList(): ResponseData<InviteCodeResp> {
        return service.fetchInviteCode()
    }

    suspend fun generateIC(): ResponseData<Boolean> {
        return service.genInviteCode()
    }

    suspend fun getInviteRecordDetail(): ResponseData<InviteDetail> {
        return service.inviteDetail()
    }

    suspend fun getNoticeMsg(): ResponseData<List<NoticeBean>> {
        return service.fetchNotice()
    }
}