package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.State
import com.bol.katalog.security.PermissionManager

data class Registry(
    val context: AggregateContext,
    private val permissionManager: PermissionManager
) : State {
    internal val namespaces = NamespaceRegistry(context, permissionManager)
    internal val schemas = SchemaRegistry(context, permissionManager)
    internal val versions = VersionRegistry(context, permissionManager)
    internal val artifacts = ArtifactRegistry(context, permissionManager)
}
