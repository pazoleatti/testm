package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Класс инкапсулирующий данный о строке из списка справочников
 */
public class RefBookListResult {
    private String refBookName;
    private boolean isReadOnly;
    private int refBookType;
    private Long refBookId;

    public String getRefBookName() {
        return refBookName;
    }

    public void setRefBookName(String refBookName) {
        this.refBookName = refBookName;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    public int getRefBookType() {
        return refBookType;
    }

    public void setRefBookType(int refBookType) {
        this.refBookType = refBookType;
    }

    public Long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }
}