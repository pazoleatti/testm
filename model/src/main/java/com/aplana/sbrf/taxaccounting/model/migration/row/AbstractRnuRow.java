package com.aplana.sbrf.taxaccounting.model.migration.row;

import java.io.Serializable;

/**
 * Модель с общими полями для строчек РНУ
 * @author Alexander Ivanov
 */
public class AbstractRnuRow implements Serializable {

    private static final long serialVersionUID = -2499576913367192605L;
    public static final char SEP = '|';

    public static String getString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private String typeRow;
    private Long num;

    protected AbstractRnuRow() {
    }

    public String getTypeRow() {
        return typeRow;
    }

    public void setTypeRow(String typeRow) {
        this.typeRow = typeRow;
    }

    public Long getNum() {
        return num;
    }

    public void setNum(Long num) {
        this.num = num;
    }

    public String toRow() {
        return "";
    }
}
