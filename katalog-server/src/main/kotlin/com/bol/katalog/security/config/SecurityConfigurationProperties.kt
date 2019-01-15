package com.bol.katalog.security.config

import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserDirectoryRole
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "katalog.security")
class SecurityConfigurationProperties {
    var auth = AuthProperties()
    var users: Map<String, UserProperties> = mutableMapOf()
    var groups = mutableMapOf<String, String>()

    class AuthProperties {
        lateinit var type: AuthType
        var oauth2 = OAuth2Properties()
    }

    class UserProperties {
        lateinit var username: String
        lateinit var password: String
        lateinit var email: String
        var roles = mutableSetOf<UserDirectoryRole>()
        var groups = mutableMapOf<String, List<GroupPermission>>()
    }
}

class OAuth2Properties {
    lateinit var registrationId: String
    lateinit var clientId: String
    lateinit var clientSecret: String
    lateinit var authorizationUri: String
    lateinit var tokenUri: String
    lateinit var userInfoUri: String
    lateinit var jwkSetUri: String
    lateinit var userNameAttributeName: String
    lateinit var userIdAttributeName: String
}