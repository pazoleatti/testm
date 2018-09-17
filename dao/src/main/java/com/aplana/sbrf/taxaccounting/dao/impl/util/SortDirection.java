package com.aplana.sbrf.taxaccounting.dao.impl.util;

public enum SortDirection {
    ASC(true) {
        public SortDirection opposite() {
            return SortDirection.DESC;
        }
    },
    DESC(false) {
        public SortDirection opposite() {
            return SortDirection.ASC;
        }
    };

    private final boolean asc;

    SortDirection(boolean asc) {
        this.asc = asc;
    }

    public abstract SortDirection opposite();

    public boolean isAsc() {
        return this.asc;
    }

    public static SortDirection of(boolean asc) {
        if (asc) {
            return SortDirection.ASC;
        } else {
            return SortDirection.DESC;
        }
    }

    public static SortDirection of(String value) {
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
