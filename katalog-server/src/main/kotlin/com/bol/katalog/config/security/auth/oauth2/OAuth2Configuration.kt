package com.bol.katalog.config.security.auth.oauth2

import com.bol.katalog.config.security.SecurityConfigurationProperties
import com.bol.katalog.config.security.ServerHttpSecurityCustomizer
import com.bol.katalog.security.KatalogUserDetailsHolder
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.AuthenticationException
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
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

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
                    .authenticationEntryPoint(RedirectToLoginEntryPoint("/oauth2/authorization/${registrations[0].registrationId}"))
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
    fun wrappingOAuth2UserService(): ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {
        val defaultService = DefaultReactiveOAuth2UserService()
        return ReactiveOAuth2UserService { userRequest ->
            defaultService.loadUser(userRequest)
                .map { it ->
                    KatalogUserDetailsHolder(
                        it.attributes["email"]!! as String,
                        "",
                        it.authorities,
                        emptyList() // get groups from somewhere...
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

    class RedirectToLoginEntryPoint(private val location: String) : ServerAuthenticationEntryPoint {
        override fun commence(exchange: ServerWebExchange, authException: AuthenticationException): Mono<Void> {
            return Mono.fromRunnable {
                exchange.response.headers.set("x-redirect", location)
                exchange.response.statusCode = HttpStatus.FORBIDDEN
            }
        }
    }
}
