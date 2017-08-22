package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Состояние ЭД
 *
 * @author dloshkarev
 */
public class RefBookDocState extends RefBookSimple<Long> {
    //Наименование состояния
    private String name;
    //Код формы по КНД
    private String knd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKnd() {
        return knd;
    }

    public void setKnd(String knd) {
        this.knd = knd;
    }
}
