package com.bol.katalog.security.tokens.auth

import com.bol.katalog.users.UserId
import org.springframework.security.core.Authentication

interface TokenService {
    suspend fun authenticate(token: String): Authentication?
    suspend fun issueToken(issuer: UserId, subjectId: UserId): String
}