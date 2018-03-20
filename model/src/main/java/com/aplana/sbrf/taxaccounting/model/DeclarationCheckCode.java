package com.aplana.sbrf.taxaccounting.model;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Список кодов для проверок форм. Используется для проверки настроек фатальности проверок
 * @author dloshkarev
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DeclarationCheckCode {
    RNU_VALUE_CONDITION("000-0007-00001"),
    RNU_CITIZENSHIP("001-0001-00002"),
    RNU_SECTION_3_10("003-0001-00002"),
    RNU_SECTION_3_10_2("003-0001-00003"),
    RNU_SECTION_3_16("003-0001-00006"),
    RNU_SECTION_2_15("004-0001-00004"),
    RNU_SECTION_2_16("004-0001-00005"),
    RNU_SECTION_2_17("004-0001-00006"),
    RNU_SECTION_2_21("004-0001-00010")
    ;

    private String code;

    DeclarationCheckCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Получение по коду
     */
    public static DeclarationCheckCode fromCode(String code) {
        for(DeclarationCheckCode checkCode: values()) {
            if (checkCode.getCode().equals(code)) {
                return checkCode;
            }
        }
        throw new IllegalArgumentException("Wrong DeclarationCheckCode code: " + code);
    }
}
