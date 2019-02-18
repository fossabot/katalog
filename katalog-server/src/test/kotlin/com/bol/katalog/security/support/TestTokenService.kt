package com.bol.katalog.security.support

import com.bol.katalog.features.registry.NamespaceId
import com.bol.katalog.security.tokens.auth.TokenService
import com.bol.katalog.users.UserId
import org.springframework.security.core.Authentication
import java.time.Instant

class TestTokenService : TokenService {
    override suspend fun authenticate(token: String): Authentication? {
        throw NotImplementedError()
    }

    override suspend fun issueToken(
        issuer: UserId,
        subjectId: UserId,
        namespaceId: NamespaceId,
        createdOn: Instant
    ): String {
        return "jwt-$issuer-$subjectId-$namespaceId"
    }
}