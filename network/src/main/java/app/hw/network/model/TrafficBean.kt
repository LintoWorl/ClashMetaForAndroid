package app.hw.network.model

data class TrafficBean(
    var u: Long = 0,//上行流量
    var d: Long = 0,//下行流量
    var record_at: Long = 0,//记录时间
    var user_id: Int = 0,
    var server_rate: String = "1.00"
)
