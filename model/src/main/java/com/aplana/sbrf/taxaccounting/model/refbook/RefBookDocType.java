package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "Коды документов"
 *
 * @author dloshkarev
 */
public class RefBookDocType extends RefBookSimple<Long> {
    //Наименование
    private String name;
    //Код
    private String code;
    //Приоритет
    private Integer priority;

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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
