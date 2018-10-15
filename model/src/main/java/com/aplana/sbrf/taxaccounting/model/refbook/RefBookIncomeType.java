package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "Коды видов доходов"
 *
 * @author dloshkarev
 */
public class RefBookIncomeType extends RefBookSimple<Long> {
    //Код
    private String code;
    //Наименование дохода
    private String name;
    //Включается в Приложение 2
    private boolean app2Include;

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

    public boolean isApp2Include() {
        return app2Include;
    }

    public void setApp2Include(boolean app2Include) {
        this.app2Include = app2Include;
    }
}
