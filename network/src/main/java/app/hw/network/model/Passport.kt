package app.hw.network.model

class CheckStat {
    var is_login: Boolean = false
}

class LoginResp {
    var token: String = ""
    var is_admin: Any? = null
    var auth_data: String = ""
}

/**
 * {
 *   "data": {
 *     "email": "xxxx@xx.com",
 *     "transfer_enable": 0,
 *     "last_login_at": null,
 *     "created_at": 1234567890,
 *     "banned": 0,
 *     "remind_expire": 0,
 *     "remind_traffic": 0,
 *     "expired_at": 0,
 *     "balance": 0,
 *     "commission_balance": 0,
 *     "plan_id": null,
 *     "discount": null,
 *     "commission_rate": null,
 *     "telegram_id": null,
 *     "uuid": "xxxxxx-xxxx-xxxx-xxxx-xxxxxx",
 *     "avatar_url": "https://xxxx.com/xxx.xxx"
 *   }
 * }
 */
class UserInfo {
    var email: String = ""
    var transfer_enable: Long = 0
    var last_login_at: Long = 0//最后登录时间，时间戳
    var created_at: Long = 0//创建时间，时间戳
    var banned: Int = 0//是否封禁使用
    var remind_expire: Long = 0//到期邮件提醒
    var remind_traffic: Long = 0//到期流量提醒
    var expired_at: Long = 0//过期时间，时间戳
    var balance: Long = 0//用户余额
    var commission_balance: Long = 0//佣金余额
    var plan_id: Int = -1//当前订阅id
    var discount: Float? = null//消费折扣
    var commission_rate: Float? = null//佣金率
    var telegram_id: String = ""//绑定TG id
    var uuid: String = ""//唯一uuid
    var avatar_url: String = ""//用户头像地址
}