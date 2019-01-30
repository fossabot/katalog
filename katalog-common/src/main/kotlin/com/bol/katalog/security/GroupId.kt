package com.bol.katalog.security

import com.fasterxml.jackson.annotation.JsonValue

data class GroupId(@get:JsonValue val value: String)
