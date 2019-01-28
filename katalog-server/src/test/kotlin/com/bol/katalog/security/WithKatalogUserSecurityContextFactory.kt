package com.bol.katalog.security

import com.bol.katalog.cqrs.Aggregate
import kotlinx.coroutines.runBlocking
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithKatalogUserSecurityContextFactory(private val security: Aggregate<SecurityState>) :
    WithSecurityContextFactory<WithKatalogUser> {
    override fun createSecurityContext(annotation: WithKatalogUser): SecurityContext {
        return object : SecurityContext {
            override fun setAuthentication(authentication: Authentication?) {
                //throw NotImplementedError()
            }

            override fun getAuthentication(): Authentication {
                val user = runBlocking { security.read { findUserByUsername(annotation.username) } }
                    ?: throw NullPointerException("Could not find user '${annotation.username}' in WithKatalogUserSecurityContextFactory")
                val principal = KatalogUserDetailsHolder(user)
                return object : AbstractAuthenticationToken(user.authorities) {
                    override fun getCredentials(): Any {
                        throw NotImplementedError()
                    }

                    override fun getPrincipal() = principal
                }
            }
        }
    }
}