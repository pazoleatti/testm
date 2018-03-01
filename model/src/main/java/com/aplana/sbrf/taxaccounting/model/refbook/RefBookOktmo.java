package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник ОКТМО
 * @author dloshkarev
 */
public class RefBookOktmo extends RefBookSimple<Long> {
    // Код
    private String code;
    // Наименование
    private String name;
    // Раздел
    private Integer section;

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

    public Integer getSection() {
        return section;
    }

    public void setSection(Integer section) {
        this.section = section;
    }
}
