package com.bol.katalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

class ParsedToken {
    private final String namespace;

    ParsedToken(String jwtToken) {
        try {
            final String[] splitToken = jwtToken.split("\\.");
            final String json = new String(Base64.getDecoder().decode(splitToken[0]));
            final Map<String, String> map = new ObjectMapper().readValue(json, new TypeReference<Map<String, String>>() {
            });
            namespace = map.get("namespace");
        } catch (Exception e) {
            throw new RuntimeException("Could not parse provided JWT token. Make sure it is valid.");
        }
    }

    String getNamespace() {
        return namespace;
    }
}
