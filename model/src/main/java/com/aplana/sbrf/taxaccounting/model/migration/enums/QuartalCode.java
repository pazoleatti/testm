package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие номера квартала к значению в новой системе
 */
public enum QuartalCode implements Serializable {
    ONE_1(1, "1", "D", "q03"),
    ONE_2(2, "2", "D", "q03"),
    ONE_3(3, "3", "D", "q03"),
    TWO_4(4, "4", "E", "q06"),
    TWO_5(5, "5", "E", "q06"),
    TWO_6(6, "6", "E", "q06"),
    THREE_7(7, "7", "F", "q09"),
    THREE_8(8, "8", "F", "q09"),
    THREE_9(9, "9", "F", "q09"),
    FOUR_10(10, "A", "G", "q12"),
    FOUR_11(11, "B", "G", "q12"),
    FOUR_12(12, "C", "G", "q12");

    private static final long serialVersionUID = 1L;

    private final int num;
    private final String codeIfMonth;
    private final String codeIfQuartal;
    private final String codeString;

    private QuartalCode(int num, String codeIfMonth, String codeIfQuartal, String codeString) {
        this.num = num;
        this.codeIfMonth = codeIfMonth;
        this.codeIfQuartal = codeIfQuartal;
        this.codeString = codeString;
    }

    public static QuartalCode fromNum(int num) {
        for (QuartalCode t : values()) {
            if (t.num == num) {
                return t;
            }
        }
        throw new IllegalArgumentException("Неверный номер квартала: " + num);
    }

    public int getNum() {
        return num;
    }

    public String getCodeIfMonth() {
        return codeIfMonth;
    }

    public String getCodeIfQuartal() {
        return codeIfQuartal;
    }

    public String getCodeString() {
        return codeString;
    }
}
