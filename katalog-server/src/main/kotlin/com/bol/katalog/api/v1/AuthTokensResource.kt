package com.bol.katalog.api.v1

import com.bol.katalog.api.PaginationRequest
import com.bol.katalog.api.SortingRequest
import com.bol.katalog.api.paginate
import com.bol.katalog.api.sort
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.send
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.monoWithUserId
import com.bol.katalog.security.tokens.*
import com.bol.katalog.users.GroupPermission
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/auth/tokens")
@PreAuthorize("hasRole('USER')")
class AuthTokensResource(
    private val context: AggregateContext,
    private val tokens: TokensAggregate
) {
    object Requests {
        data class NewToken(
            val description: String,
            val groupId: GroupId,
            val permissions: Set<GroupPermission>
        )
    }

    object Responses {
        data class Token(val id: TokenId, val description: String, val createdOn: Instant)
        data class TokenCreated(val id: TokenId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun issueToken(@RequestBody data: Requests.NewToken) = monoWithUserId {
        val tokenId: TokenId = UUID.randomUUID().toString()

        context.send(
            IssueTokenCommand(
                id = tokenId,
                description = data.description,
                subjectId = UUID.randomUUID().toString(),
                groupId = data.groupId,
                permissions = data.permissions
            )
        )

        Responses.TokenCreated(tokenId)
    }

    @GetMapping
    fun getTokens(
        pagination: PaginationRequest,
        sorting: SortingRequest
    ) = monoWithUserId {
        tokens.getAllTokens().sort(sorting) { column ->
            when (column) {
                "description" -> {
                    { it.id }
                }
                "createdOn" -> {
                    { it.createdOn }
                }
                else -> {
                    { it.id }
                }
            }
        }.paginate(pagination) {
            toResponse(it)
        }
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: TokenId
    ) = monoWithUserId {
        toResponse(tokens.getById(userId!!, id))
    }

    private fun toResponse(token: Token) = Responses.Token(token.id, token.description, token.createdOn)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: TokenId
    ) = monoWithUserId {
        context.send(RevokeTokenCommand(id))
    }
}