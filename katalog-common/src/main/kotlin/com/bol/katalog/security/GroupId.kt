package com.bol.katalog.security

import com.fasterxml.jackson.annotation.JsonValue

// Should replace the Jackson annotation with a mixin so we don't need the Jackson dependency
data class GroupId(@get:JsonValue val value: String)
