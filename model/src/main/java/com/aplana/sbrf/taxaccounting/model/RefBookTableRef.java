package com.aplana.sbrf.taxaccounting.model;

/**
 * Хранит отношение RefBook и его табличной части
 */
public enum RefBookTableRef {
    REF_BOOK_99(TaxType.PROPERTY, 99L, new long[]{206L});

    private RefBookTableRef(TaxType taxType, long refBookId, long[] refBookTable) {
        this.taxType = taxType;
        this.refBookId = refBookId;
        this.refBookTable = refBookTable;

    }

    public long getRefBookId() {
        return refBookId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public long[] getRefBookTable() {
        return refBookTable;
    }

    public static long[] getTablesIdByRefBook(long refBookId) {
        for (RefBookTableRef r: values()) {
            if (r.refBookId == refBookId) {
                return r.getRefBookTable();
            }
        }
        return null;
    }

    private final long refBookId;
    private final TaxType taxType;
    private final long[] refBookTable;
}
