package app.hw.network.api

import app.hw.network.RetrofitManager
import app.hw.network.model.InviteCodeResp
import app.hw.network.model.InviteDetail
import app.hw.network.model.NoticeBean
import app.hw.network.util.NetworkUtil
import com.github.kr328.clash.common.Global


object OthersApi {

    private val service: OthersService by lazy {
        RetrofitManager.createService(OthersService::class.java)
    }
    private val service2: OthersService by lazy {
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
        if (NetworkUtil.isVpnRunning(Global.application)) {
            return service2.fetchNotice()
        }
        return service.fetchNotice()
    }

    suspend fun checkVersion(userToken: String): ResponseData<String> {
        return if (NetworkUtil.isVpnRunning(Global.application)) {
            service2.checkAppVer(userToken)
        } else {
            service.checkAppVer(userToken)
        }
    }
}