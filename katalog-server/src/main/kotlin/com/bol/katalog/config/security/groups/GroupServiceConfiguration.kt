package com.bol.katalog.config.security.groups

import com.bol.katalog.security.groups.CompositeGroupService
import com.bol.katalog.security.groups.GroupProvider
import com.bol.katalog.security.groups.GroupService
import com.bol.katalog.security.tokens.JwtGroupProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GroupServiceConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun compositeGroupService(providers: List<GroupProvider>): GroupService {
        return CompositeGroupService(providers)
    }

    @Bean
    fun jwtGroupProvider(): GroupProvider = JwtGroupProvider()
}