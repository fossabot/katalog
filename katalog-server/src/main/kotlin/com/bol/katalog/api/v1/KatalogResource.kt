package com.bol.katalog.api.v1

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/katalog")
class KatalogResource {
    object Responses {
        data class Version(val version: String)
    }

    @GetMapping("/version")
    fun getVersion() = Responses.Version("0.1.0") // Get this from Git tags at some point
}