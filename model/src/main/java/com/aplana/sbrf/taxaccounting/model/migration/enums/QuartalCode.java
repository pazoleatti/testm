package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие номера квартала к значению в новой системе
 */
public enum QuartalCode implements Serializable {
    ONE_1(1, 21),
    ONE_2(2, 21),
    ONE_3(3, 21),
    TWO_4(4, 31),
    TWO_5(5, 31),
    TWO_6(6, 31),
    THREE_7(7, 33),
    THREE_8(8, 33),
    THREE_9(9, 33),
    FOUR_10(10, 34),
    FOUR_11(11, 34),
    FOUR_12(12, 34);

    private static final long serialVersionUID = 1L;

    private final int num;
    private final int code;

    private QuartalCode(int num, int code) {
        this.num = num;
        this.code = code;
    }

    public static int fromNum(int num) {
        for (QuartalCode t : values()) {
            if (t.num == num) {
                return t.code;
            }
        }
        throw new IllegalArgumentException("Неверный номер квартала: " + num);
    }

    public int getNum() {
        return num;
    }

    public int getCode() {
        return code;
    }
}
