package com.bol.katalog.config.security.auth.oauth2

import com.bol.katalog.config.security.SecurityConfigurationProperties
import com.bol.katalog.config.security.ServerHttpSecurityCustomizer
import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.groups.GroupService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint

@Configuration
@ConditionalOnProperty("katalog.security.auth.type", havingValue = "OAUTH2", matchIfMissing = false)
class OAuth2Configuration {
    @Bean
    @ConditionalOnMissingBean
    fun oauth2SecurityWebFilterChain(
        http: ServerHttpSecurity,
        registrations: List<ClientRegistration>
    ) = object : ServerHttpSecurityCustomizer {
        override fun customize(http: ServerHttpSecurity) {
            http
                .oauth2Login()

            if (registrations.size == 1) {
                http
                    .exceptionHandling()
                    .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.FORBIDDEN))
            } else {
                throw RuntimeException("Currently only one concurrent OAuth2 client registration is supported")
            }
        }
    }

    @Bean
    fun reactiveClientRegistrationRepository(clientRegistration: ClientRegistration): ReactiveClientRegistrationRepository {
        return InMemoryReactiveClientRegistrationRepository(clientRegistration)
    }

    @Bean
    fun wrappingOAuth2UserService(
        configuration: SecurityConfigurationProperties,
        groupService: GroupService
    ): ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {
        val defaultService = DefaultReactiveOAuth2UserService()
        return ReactiveOAuth2UserService { userRequest ->
            defaultService.loadUser(userRequest)
                .map { it ->
                    KatalogUserDetailsHolder(
                        it.attributes[configuration.auth.oauth2.userNameAttribute]!! as String,
                        "",
                        it.authorities
                    )
                }
        }
    }

    @Bean
    fun clientRegistration(
        properties: SecurityConfigurationProperties
    ): ClientRegistration = with(properties.auth.oauth2) {
        ClientRegistration.withRegistrationId(registrationId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "name", "profile", "email")
            .authorizationUri(authorizationUri)
            .tokenUri(tokenUri)
            .userInfoUri(userInfoUri)
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .jwkSetUri(jwkSetUri)
            .clientName("Katalog")
            .userNameAttributeName("name")
            .build()
    }
}
