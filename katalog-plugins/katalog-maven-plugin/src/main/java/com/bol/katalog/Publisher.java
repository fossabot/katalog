package com.bol.katalog;

import org.apache.maven.shared.model.fileset.util.FileSetManager;

import java.io.File;

class Publisher {
    private FileSetManager fileSetManager = new FileSetManager();
    private KatalogApi client;
    private ParsedToken token;

    Publisher(KatalogApi client, ParsedToken token) {
        this.client = client;
        this.token = token;
    }

    void publish(Specification spec) {
        final String[] files = fileSetManager.getIncludedFiles(spec.getFileset());

        String versionId;
        try {
            final KatalogApi.FindVersionResponse foundVersion = client.findVersion(token.getNamespace(), spec.getSchema(), spec.getVersion());
            versionId = foundVersion.versionId;
        } catch (NotFoundException e) {
            final KatalogApi.FindSchemaResponse foundSchema = client.findSchema(token.getNamespace(), spec.getSchema());
            final KatalogApi.CreateVersionResponse createdVersion = client.createVersion(foundSchema.schemaId, spec.getVersion());
            versionId = createdVersion.versionId;
        }

        for (String filename : files) {
            final File file = new File(spec.getFileset().getDirectory(), filename);
            client.createArtifact(versionId, file);
        }
    }
}
