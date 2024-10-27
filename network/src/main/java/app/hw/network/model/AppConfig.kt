package app.hw.network.model

/**
 * {
 *   "data": {
 *     "tos_url": "https://xxx.com",
 *     "is_email_verify": 0,
 *     "is_invite_force": 0,
 *     "email_whitelist_suffix": 0,
 *     "is_recaptcha": 0,
 *     "recaptcha_site_key": "xxx",
 *     "app_description": "Hallo!",
 *     "app_url": "https://xxx.com"
 *   }
 * }
 */
class AppConfig {
    var tos_url: String? = ""
    var is_email_verify: Int = 0
    var is_invite_force: Int = 0
    var email_whitelist_suffix: Any? = null//emptyList<String>()
    var is_recaptcha: Int = 0
    var recaptcha_site_key: String = ""
    var app_description: String = ""
    var app_url: String = ""
    var logo: String = ""
}