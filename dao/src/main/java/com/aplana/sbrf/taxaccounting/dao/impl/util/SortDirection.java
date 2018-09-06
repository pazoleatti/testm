package com.aplana.sbrf.taxaccounting.dao.impl.util;

public enum SortDirection {
    ASC(true),
    DESC(false);

    private final boolean asc;

    SortDirection(boolean asc) {
        this.asc = asc;
    }

    public boolean isAsc() {
        return this.asc;
    }

    public static SortDirection getByAsc(boolean asc) {
        if (asc) {
            return SortDirection.ASC;
        } else {
            return SortDirection.DESC;
        }
    }

    public static SortDirection getByValue(String value) {
        if (value == null) {
            return SortDirection.ASC;
        }
        if (value.toLowerCase().equals("desc")) {
            return SortDirection.DESC;
        }
        return SortDirection.ASC;
    }

    public String toString() {
        return this.name().toLowerCase();
    }
}
