package com.bol.katalog.api.v1

import org.springframework.boot.info.GitProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/katalog")
class KatalogResource(
    private val git: GitProperties?
) {
    object Responses {
        data class Version(
            val version: String,
            val buildTime: Instant
        )
    }

    @GetMapping("/version")
    fun getVersion() = if (git != null) {
        Responses.Version(
            git.get("build.version"),
            Instant.ofEpochMilli(git.get("build.time").toLong())
        )
    } else {
        Responses.Version("Unknown", Instant.now())
    }
}