package com.bol.katalog

import com.bol.katalog.config.KatalogConfigurationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(KatalogConfigurationProperties::class)
class KatalogApplication

fun main(args: Array<String>) {
    runApplication<KatalogApplication>(*args)
}