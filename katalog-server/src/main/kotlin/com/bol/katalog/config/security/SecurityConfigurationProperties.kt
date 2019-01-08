package com.bol.katalog.config.security

import com.bol.katalog.domain.GroupPermission
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "katalog.security")
class SecurityConfigurationProperties {
    var auth = AuthProperties()
    var users = UserProperties()
    var groups = GroupProperties()
    var token = TokenProperties()

    class AuthProperties {
        lateinit var type: AuthType
        var oauth2 = OAuth2Properties()
    }

    class UserProperties {
        var simple = SimpleUserProperties()
    }

    class GroupProperties {
        var simple = SimpleGroupProperties()
    }

    class TokenProperties {
        lateinit var hmacShaKey: String
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
}

class SimpleUserProperties {
    var enabled: Boolean = false
    var users: Map<String, UserProperties> = mutableMapOf()

    class UserProperties {
        lateinit var username: String
        lateinit var password: String
        var roles = mutableListOf<String>()
        var groups = mutableMapOf<String, List<GroupPermission>>()
    }
}

class SimpleGroupProperties {
    var enabled: Boolean = false
    var groups = mutableListOf<String>()
}
