package com.bol.katalog.security.support

import com.bol.katalog.security.tokens.auth.TokenService
import com.bol.katalog.users.UserId
import org.springframework.security.core.Authentication

class TestTokenService : TokenService {
    override suspend fun authenticate(token: String): Authentication? {
        throw NotImplementedError()
    }

    override suspend fun issueToken(issuer: UserId, subjectId: UserId): String {
        return "jwt-$issuer-$subjectId"
    }
}