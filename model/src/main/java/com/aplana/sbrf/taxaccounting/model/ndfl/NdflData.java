package com.aplana.sbrf.taxaccounting.model.ndfl;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.io.Serializable;

/**
 * Абстрактный класс, представляющий объектданных форм НДФЛ и имеющий идентификатор
 * и наименование АСНУ источника
 *
 * @param <idType> тип идентификатора объекта
 */
public abstract class NdflData<idType extends Number> extends IdentityObject<idType> implements Serializable {

    /**
     * Наименование АСНУ
     */
    private String asnuName;

    public String getAsnuName() {
        return asnuName;
    }

    public void setAsnuName(String asnuName) {
        this.asnuName = asnuName;
    }
}
