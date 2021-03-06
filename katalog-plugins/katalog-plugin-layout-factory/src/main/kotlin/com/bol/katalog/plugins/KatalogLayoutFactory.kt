package com.bol.katalog.plugins

import org.springframework.boot.loader.tools.Layout
import org.springframework.boot.loader.tools.LayoutFactory
import java.io.File

class KatalogLayoutFactory : LayoutFactory {
    var name = "katalog"

    constructor() {}

    constructor(name: String) {
        this.name = name
    }

    override fun getLayout(source: File): Layout {
        return KatalogLayout()
    }
}
