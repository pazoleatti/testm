package com.aplana.sbrf.taxaccounting.model.ndfl;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Абстрактный класс, представляющий объектданных форм НДФЛ и имеющий идентификатор
 * и наименование АСНУ источника
 *
 * @param <idType> тип идентификатора объекта
 */
public abstract class NdflData<idType extends Number> extends IdentityObject<idType> {

    /**
     * Наименование АСНУ
     */
    private String asnu;

    public String getAsnu() {
        return asnu;
    }

    public void setAsnu(String asnu) {
        this.asnu = asnu;
    }
}
