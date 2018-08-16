package com.bol.blueprint

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "blueprint")
class BlueprintConfigurationProperties {
    var security: SecurityProperties = SecurityProperties()

    class SecurityProperties {
        var simple: SimpleProperties = SimpleProperties()

        class SimpleProperties {
            var enabled: Boolean = false

            var users: Map<String, UserProperties> = mutableMapOf()

            class UserProperties {
                lateinit var username: String
                lateinit var password: String
                lateinit var roles: List<String>
            }
        }
    }
}