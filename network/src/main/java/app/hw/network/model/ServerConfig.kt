package app.hw.network.model

data class ServerConfig(
    var license: String,
    var apiUrl: String,
    var crispId: String,
    var inviteUrl: String,
    var nodeDelayShow: DelayBean,
    var trafficLogShow: Boolean,
    var trafficUnlimited: TrafficInfo,
    var agreements: Agreement,
)

data class DelayBean(
    var type: Int,
    var colorBest: Long,
    var colorGood: Long
)

data class TrafficInfo(
    var value: Long,
    var text: String
)

data class Agreement(
    var show: Boolean,
    var title: String,
    var content: String,
    var serviceLink: String,
    var privacyLink: String
)