package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Категории прикрепляемых файлов
 * Created by aokunev on 08.08.2017.
 */
public class RefBookAttachFileType extends RefBookSimple<Long> {
    //Код категории
    byte code;
    //Наименование
    String name;

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
