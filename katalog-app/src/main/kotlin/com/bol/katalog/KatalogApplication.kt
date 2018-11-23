package com.bol.katalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KatalogApplication

fun main(args: Array<String>) {
    runApplication<KatalogApplication>(*args)
}