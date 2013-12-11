package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие номера квартала к значению в новой системе
 * применяется для формирования информации о ТФ
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

    private final int num;              // номер месяца
    private final String codeIfMonth;   // символ обозначения месяца для RNU
    private final String codeIfQuartal; // симмвол обозначения квартала для RNU
    private final String codeString;    // обозначения квартала в XML

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
        throw new IllegalArgumentException("Is not contained in enum QuartalCode!  num: " + num);
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
