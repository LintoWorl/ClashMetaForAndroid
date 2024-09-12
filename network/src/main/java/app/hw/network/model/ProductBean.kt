package app.hw.network.model

import androidx.core.view.ContentInfoCompat.Flags

/**
 * {
 *   "data": {
 *     "plan_id": null,
 *     "token": "xxx",
 *     "expired_at": 0,
 *     "u": 0,
 *     "d": 0,
 *     "transfer_enable": 0,
 *     "email": "xxx@xxx.com",
 *     "subscribe_url": "https://xxx.com/api/v1/client/subscribe?token=xxx",
 *     "reset_day": null
 *   }
 * }
 */
data class ProductSubsInfo(
    var plan_id: String,
    var token: String = "",
    var expired_at: Long = 0,
    var u: Long = 0,//已用上行流量
    var d: Long = 0,//已用下行流量
    var transfer_enable: Long = 0,//总可用流量
    var email: String = "",
    var plan: SubsProductBean? = null,
    var subscribe_url: String = "",
    var reset_day: Int = 1//重置日
)

/**
 * {
 *   "data": [
 *     {
 *       "id": 1,
 *       "group_id": 1,
 *       "transfer_enable": 100,
 *       "name": "Plan name",
 *       "show": 1,
 *       "sort": null,
 *       "renew": 1,
 *       "content": "xxx",
 *       "month_price": 10,
 *       "quarter_price": null,
 *       "half_year_price": null,
 *       "year_price": 20000,
 *       "two_year_price": null,
 *       "three_year_price": null,
 *       "onetime_price": null,
 *       "reset_price": 1000,
 *       "reset_traffic_method": null,
 *       "created_at": 1234567890,
 *       "updated_at": 1234567890
 *     }
 *   ]
 * }
 */
data class SubsProductBean(
    var id: Int = 0,
    var group_id: Long = 0,
    var transfer_enable: Long = 0,
    var speed_limit: String? = null,
    var name: String = "",
    var show: Int = 0,
    var sort: String? = "",
    var renew: Int = 0,
    var content: String? = "",//套餐描述
    var month_price: Long? = 0,
    var quarter_price: Long? = null,
    var half_year_price: Long? = null,
    var year_price: Long? = null,
    var two_year_price: Long? = null,
    var three_year_price: Long? = null,
    var onetime_price: Long? = null,
    var reset_price: Long? = null,
    var reset_traffic_method: String? = null,
    var capacity_limit: Long? = null,
    var created_at: Long = 0,
    var updated_at: Long = 0
)