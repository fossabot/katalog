package com.bol.katalog.testing

import org.springframework.core.ParameterizedTypeReference

inline fun <reified T> ref() = object : ParameterizedTypeReference<T>() {}
