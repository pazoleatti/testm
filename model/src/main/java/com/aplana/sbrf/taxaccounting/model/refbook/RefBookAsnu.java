package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * АСНУ
 *
 * @author dloshkarev
 */
public class RefBookAsnu extends RefBookSimple<Long> {
    //Наименование АСНУ
    private String name;
    //Код АСНУ
    private String code;
    //Тип дохода
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
