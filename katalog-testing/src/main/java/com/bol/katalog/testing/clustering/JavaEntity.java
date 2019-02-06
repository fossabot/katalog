package com.bol.katalog.testing.clustering;

import java.util.List;
import java.util.Objects;

public class JavaEntity {
    private final String value;
    private final List<String> list;

    JavaEntity(String value, List<String> list) {
        this.value = value;
        this.list = list;
    }

    public String getValue() {
        return value;
    }

    public List<String> getList() {
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaEntity that = (JavaEntity) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, list);
    }
}
