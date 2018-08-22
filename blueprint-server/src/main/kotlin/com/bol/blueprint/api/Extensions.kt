package com.bol.blueprint.api

import com.bol.blueprint.domain.MediaType

fun org.springframework.http.MediaType.toMediaType(): MediaType = when {
    this.isCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON) -> MediaType.JSON
    this.isCompatibleWith(org.springframework.http.MediaType.APPLICATION_XML) -> MediaType.XML
    else -> throw UnsupportedOperationException("Could not determine media type for: $this")
}