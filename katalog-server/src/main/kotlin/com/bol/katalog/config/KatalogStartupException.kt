package com.bol.katalog.config

class KatalogStartupException(description: String, val action: String, cause: Throwable? = null) :
    Throwable(description, cause)
