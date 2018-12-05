package com.bol.katalog.config

import com.bol.katalog.domain.GroupPermission
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "katalog")
class KatalogConfigurationProperties {
    var testData = TestDataProperties()
    var security = SecurityProperties()

    class TestDataProperties {
        var enabled: Boolean = false
    }

    class SecurityProperties {
        var simple = SimpleProperties()

        class SimpleProperties {
            var enabled: Boolean = false

            var users: Map<String, UserProperties> = mutableMapOf()
            var groups = mutableListOf<String>()

            class UserProperties {
                lateinit var username: String
                lateinit var password: String
                var roles = mutableListOf<String>()
                var groups = mutableMapOf<String, List<GroupPermission>>()
            }
        }
    }
}