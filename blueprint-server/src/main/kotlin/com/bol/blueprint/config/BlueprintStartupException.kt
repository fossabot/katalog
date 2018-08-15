package com.bol.blueprint.config

class BlueprintStartupException(description: String, val action: String, cause: Throwable? = null) : Throwable(description, cause)
