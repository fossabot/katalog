package com.bol.blueprint.plugins

import org.springframework.boot.loader.tools.Layouts
import org.springframework.boot.loader.tools.LibraryScope

class BlueprintLayout: Layouts.None() {
    /**
     * In a Blueprint plugin the classes need to be at the root of the jar
     */
    override fun getRepackagedClassesLocation(): String {
        return ""
    }

    override fun getLibraryDestination(libraryName: String, scope: LibraryScope): String? {
        // Don't package any 'provided' dependencies
        return if (scope == LibraryScope.PROVIDED) {
            null
        } else {
            return super.getLibraryDestination(libraryName, scope)
        }
    }

}
