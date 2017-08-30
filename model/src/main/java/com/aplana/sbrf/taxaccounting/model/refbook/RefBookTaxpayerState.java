package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Статусы налогоплательщика
 *
 * @author Andrey Drunk
 */
public class RefBookTaxpayerState extends RefBookSimple<Long> {

    //Код
    private String code;
    //Наименование
    private String name;

    public RefBookTaxpayerState() {
    }

    public RefBookTaxpayerState(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[").append(id).append(", ").append(code).append("]").toString();
    }
}
