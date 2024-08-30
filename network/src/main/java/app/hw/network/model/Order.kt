package app.hw.network.model

/**
 * {
 *   "data": [
 *     {
 *       "invite_user_id": null,
 *       "plan_id": 1,
 *       "coupon_id": null,
 *       "payment_id": 10,
 *       "type": 1,
 *       "cycle": "month_price",
 *       "trade_no": "xxx",
 *       "callback_no": null,
 *       "total_amount": 10,
 *       "discount_amount": null,
 *       "surplus_amount": null,
 *       "refund_amount": null,
 *       "balance_amount": null,
 *       "surplus_order_ids": null,
 *       "status": 2,
 *       "commission_status": 0,
 *       "commission_balance": 0,
 *       "paid_at": null,
 *       "created_at": 1234567890,
 *       "updated_at": 1234567890,
 *       "plan": {
 *         "表关联"
 *       }
 *     }
 *   ]
 * }
 */
data class OrderBean(
    var invite_user_id: String = "",
    var plan_id: Long = 0,
    var coupon_id: String = "",
    var payment_id: Long = 0,
    var type: Int = 1,//订单类型 1新购2续费3升级
    var period: String = "",//套餐类型
    var trade_no: String = "",
    var callback_no: String? = null,
    var total_amount: Int = 0,
    var discount_amount: Int? = null,
    var surplus_amount: Int? = null,
    var refund_amount: Int? = null,
    var balance_amount: Int? = null,
    var surplus_order_ids: Int? = null,
    var status: Int = 0,//订单状态 0待支付1开通中2已取消3已完成4已折抵
    var commission_status: Int = 0,//佣金状态 0待确认1发放中2有效3无效
    var commission_balance: Int = 0,
    var paid_at: Long? = null,
    var created_at: Long = 0,
    var updated_at: Long = 0,
    var plan: SubsProductBean? = null,
    var try_out_plan_id: Long = 0
)

class OrderStatus {
    fun getStatusDesc(no: Int): String {
        return when (no) {
            1 -> "开通中"
            2 -> "已取消"
            3 -> "已完成"
            4 -> "已折抵"
            else -> "待支付"
        }
    }
}

/**
 * {
 *   "data": [
 *     {
 *       "id": 10,
 *       "name": "pay",
 *       "payment": "XXXPay"
 *     }
 *   ]
 * }
 */
data class PaymentBean(
    var id: Int = 0,
    var name: String = "",
    var payment: String = ""
)

data class InviteCodeResp(
    val codes: List<InviteCodeBean> = arrayListOf(),
    var stat: List<Long> = arrayListOf()
)

data class InviteCodeBean(
    var id: Long = 0,
    var user_id: Long = 0,
    var code: String = "",
    var status: Int,
    var pv: Int,
    var created_at: Long = 0,
    var updated_at: Long = 0
)

/**
 * {
 *   "id": 1,
 *   "commission_status": 1,
 *   "commission_balance": 10,
 *   "created_at": 1234567890,
 *   "updated_at": 1234567890
 * }
 */
data class InviteDetail(
    var id: Long = 0,
    var commission_status: Int = 1,
    var commission_balance: Int = 0,
    var created_at: Long = 0,
    var updated_at: Long = 0
)

data class NoticeBean(
    var id: Long = 0,
    var title: String = "",
    var content: String = "",
    var img_url: String? = null,
    var created_at: Long = 0,
    var updated_at: Long = 0
)