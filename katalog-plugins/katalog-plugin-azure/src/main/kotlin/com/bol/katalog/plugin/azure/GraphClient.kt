package com.bol.katalog.plugin.azure

import com.fasterxml.jackson.annotation.JsonProperty
import com.microsoft.aad.adal4j.AuthenticationContext
import com.microsoft.aad.adal4j.ClientCredential
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

@Component
class GraphClient(
    config: AzureProperties
) {
    private val log = KotlinLogging.logger {}

    private val restTemplate = RestTemplate()

    private val baseUrl = "https://graph.microsoft.com"

    private val context: AuthenticationContext = AuthenticationContext(
        config.graphApi.authority,
        false,
        Executors.newFixedThreadPool(1)
    )

    private val clientCredential = ClientCredential(config.graphApi.clientId, config.graphApi.clientSecret)

    fun getGroups() = getData(
        "$baseUrl/v1.0/groups?\$select=id,displayName",
        object : ParameterizedTypeReference<ODataResponse<GroupResponse>>() {}
    )

    fun getGroupMembers(groupId: String) = getData(
        "$baseUrl/v1.0/groups/$groupId/members?\$select=id,displayName",
        object : ParameterizedTypeReference<ODataResponse<GroupMembersResponse>>() {}
    )

    fun getUsers() = getData(
        "$baseUrl/v1.0/users?\$select=id,displayName,otherMails",
        object : ParameterizedTypeReference<ODataResponse<UserResponse>>() {}
    )

    private fun <T> getData(startUrl: String, ref: ParameterizedTypeReference<ODataResponse<T>>): Collection<T> {
        val result = mutableListOf<T>()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer ${acquireToken()}")
        val entity = HttpEntity<Void>(headers)

        var url = startUrl
        while (true) {
            try {
                val response: ResponseEntity<ODataResponse<T>> = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ref // need to pass this in, because Kotlin can't construct a typereference from generic reified arguments apparently
                )

                val oDataResponse = response.body ?: throw RuntimeException("Got an empty response body")
                result.addAll(oDataResponse.value)

                if (oDataResponse.nextLink == null) break
                url = UriUtils.decode(oDataResponse.nextLink, StandardCharsets.UTF_8.name())
            } catch (e: Exception) {
                log.error("Could not get data from Graph API", e)
            }
        }

        return result
    }

    data class ODataResponse<T>(
        @JsonProperty("@odata.nextLink") val nextLink: String?,
        @JsonProperty("value") val value: Collection<T>
    )

    data class GroupResponse(
        @JsonProperty("id") val id: String,
        @JsonProperty("displayName") val displayName: String
    )

    data class GroupMembersResponse(
        @JsonProperty("id") val id: String
    )

    data class UserResponse(
        @JsonProperty("id") val id: String,
        @JsonProperty("displayName") val displayName: String,
        @JsonProperty("otherMails") val otherMails: List<String> = emptyList()
    )

    fun acquireToken(): String = context.acquireToken(baseUrl, clientCredential, null).get()?.accessToken
        ?: throw RuntimeException("Could not acquire token")
}
