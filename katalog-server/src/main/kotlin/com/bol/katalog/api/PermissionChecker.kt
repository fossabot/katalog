package com.bol.katalog.api

/**
 * The permission checker can be used to see whether certain group permissions apply.
 * Since it's not always clear which group an object belongs to, the checks will 'bubble up' from a certain object
 * until we can finally check the group itself.
 *
 * The permission checker extracts the current user from either the Reactive or the Coroutine context.
 */
/*@Component("permissionChecker")
class PermissionChecker(
    val registry: Aggregate<RegistryState>,
    val security: Aggregate<SecurityState>
) {
    private val log = KotlinLogging.logger {}

    fun isAllowed(groupId: GroupId, permission: String) = runBlocking {
        getUser()?.let { user ->
            val result = security.read { hasPermission(user, groupId, GroupPermission.valueOf(permission)) }
            log.debug("User '$user' has permission '$permission' for group '$groupId': $result")
            result
        } ?: false
    }

    fun isAllowedForNamespace(namespaceId: NamespaceId, permission: String) = runBlocking {
        isAllowed(registry.read { getNamespace(namespaceId).groupId }, permission)
    }

    fun isAllowedForSchema(schemaId: SchemaId, permission: String) = runBlocking {
        isAllowedForNamespace(registry.read { getSchemaNamespaceId(schemaId) }, permission)
    }

    fun isAllowedForVersion(versionId: VersionId, permission: String) = runBlocking {
        isAllowedForSchema(registry.read { getVersionSchemaId(versionId) }, permission)
    }

    fun isAllowedForArtifact(artifactId: ArtifactId, permission: String) = runBlocking {
        isAllowedForVersion(registry.read { getArtifactVersionId(artifactId) }, permission)
    }

    fun getUser(): User? {
        val reactiveUser = runBlocking {
            val securityContext = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()
            val userDetails = securityContext?.authentication?.principal as KatalogUserDetails?
            userDetails?.getUser()
        }
        if (reactiveUser != null) return reactiveUser

        val coroutineUser = runBlocking { CoroutineUserContext.get() }
        if (coroutineUser != null) return coroutineUser

        return null
    }
}*/