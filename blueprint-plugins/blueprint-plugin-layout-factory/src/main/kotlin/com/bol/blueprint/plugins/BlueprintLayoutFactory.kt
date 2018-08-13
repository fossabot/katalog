package com.bol.blueprint.plugins

import org.springframework.boot.loader.tools.Layout
import org.springframework.boot.loader.tools.LayoutFactory
import java.io.File

class BlueprintLayoutFactory : LayoutFactory {
    var name = "blueprint"

    constructor() {}

    constructor(name: String) {
        this.name = name
    }

    override fun getLayout(source: File): Layout {
        return BlueprintLayout()
    }
}
