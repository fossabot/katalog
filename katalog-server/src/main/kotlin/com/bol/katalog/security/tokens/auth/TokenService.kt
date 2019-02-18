package com.bol.katalog.security.tokens.auth

import com.bol.katalog.users.UserId
import org.springframework.security.core.Authentication
import java.time.Instant

interface TokenService {
    suspend fun authenticate(token: String): Authentication?
    suspend fun issueToken(issuer: UserId, subjectId: UserId, namespace: String, createdOn: Instant): String
}