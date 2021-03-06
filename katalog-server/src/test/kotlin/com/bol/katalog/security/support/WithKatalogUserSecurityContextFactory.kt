package com.bol.katalog.security.support

import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.asAuthentication
import com.bol.katalog.utils.runBlockingAsSystem
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithKatalogUserSecurityContextFactory(private val security: SecurityAggregate) :
    WithSecurityContextFactory<WithKatalogUser> {
    override fun createSecurityContext(annotation: WithKatalogUser): SecurityContext {
        return object : SecurityContext {
            override fun setAuthentication(authentication: Authentication?) {
                //throw NotImplementedError()
            }

            override fun getAuthentication(): Authentication {
                val user = runBlockingAsSystem { security.findUserByUsername(annotation.username) }
                    ?: throw NullPointerException("Could not find user '${annotation.username}' in WithKatalogUserSecurityContextFactory")
                return KatalogUserDetailsHolder(user).asAuthentication()
            }
        }
    }
}