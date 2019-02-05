package com.bol.katalog.security.support

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.read
import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.Security
import com.bol.katalog.utils.runBlockingAsSystem
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithKatalogUserSecurityContextFactory(private val security: Aggregate<Security>) :
    WithSecurityContextFactory<WithKatalogUser> {
    override fun createSecurityContext(annotation: WithKatalogUser): SecurityContext {
        return object : SecurityContext {
            override fun setAuthentication(authentication: Authentication?) {
                //throw NotImplementedError()
            }

            override fun getAuthentication(): Authentication {
                val user = runBlockingAsSystem { security.read { findUserByUsername(annotation.username) } }
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