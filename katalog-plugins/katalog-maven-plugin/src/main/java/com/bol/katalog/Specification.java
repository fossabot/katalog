package com.bol.katalog;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;

import java.util.Objects;

public class Specification {
    @Parameter(required = true)
    private String schema;

    @Parameter(required = true)
    private String version;

    @Parameter(required = true)
    private FileSet fileset;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public FileSet getFileset() {
        return fileset;
    }

    public void setFileset(FileSet fileset) {
        this.fileset = fileset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Specification that = (Specification) o;
        return Objects.equals(schema, that.schema) &&
                Objects.equals(version, that.version) &&
                Objects.equals(fileset, that.fileset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, version, fileset);
    }
}
