package com.bol.blueprint

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BlueprintApplication

fun main(args: Array<String>) {
    runApplication<BlueprintApplication>(*args)
}