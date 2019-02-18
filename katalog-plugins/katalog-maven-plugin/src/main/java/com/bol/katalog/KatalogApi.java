package com.bol.katalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.io.File;

@Headers("Accept: application/json")
interface KatalogApi {
    @RequestLine("GET /api/v1/schemas/find/{namespace}/{schema}")
    @Headers("Content-Type: application/json")
    FindSchemaResponse findSchema(@Param("namespace") String namespace, @Param("schema") String schema);

    @RequestLine("GET /api/v1/versions/find/{namespace}/{schema}/{version}")
    @Headers("Content-Type: application/json")
    FindVersionResponse findVersion(@Param("namespace") String namespace, @Param("schema") String schema, @Param("version") String version);

    @RequestLine("POST /api/v1/versions")
    @Headers("Content-Type: application/json")
    CreateVersionResponse createVersion(@Param("schemaId") String schemaId, @Param("version") String version);

    @RequestLine("POST /api/v1/artifacts")
    @Headers("Content-Type: multipart/form-data")
    CreateArtifactResponse createArtifact(@Param("versionId") String versionId, @Param("file") File file);

    class FindSchemaResponse {
        String schemaId;

        @JsonCreator
        public FindSchemaResponse(@JsonProperty("id") String schemaId) {
            this.schemaId = schemaId;
        }
    }

    class FindVersionResponse {
        String versionId;

        @JsonCreator
        public FindVersionResponse(@JsonProperty("id") String versionId) {
            this.versionId = versionId;
        }
    }

    class CreateVersionResponse {
        String versionId;

        @JsonCreator
        public CreateVersionResponse(@JsonProperty("id") String versionId) {
            this.versionId = versionId;
        }
    }

    class CreateArtifactResponse {
        String artifactId;

        @JsonCreator
        public CreateArtifactResponse(@JsonProperty("id") String artifactId) {
            this.artifactId = artifactId;
        }
    }
}
