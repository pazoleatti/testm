package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;

/**
 * @author Andrey Drunk
 */
public class NaturalPerson extends RegistryPerson implements IdentityPerson {

    /**
     * Идентификатор ФЛ в первичной форме
     */
    private Long primaryPersonId;

    /**
     * Поле для хранения веса записи при идентификации
     */
    private Double weight;

    /**
     * Флаг указывающий на то что запись в справочнике не надо обновлять
     */
    private boolean needUpdate = true;

    /**
     * Система источник
     */
    private Long sourceId;

    public Long getPrimaryPersonId() {
        return primaryPersonId;
    }

    public void setPrimaryPersonId(Long primaryPersonId) {
        this.primaryPersonId = primaryPersonId;
    }

    @Override
    public Double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * Получить идентификатор ФЛ, данный метод используется при работе с ФЛ из первичных форм, так как там может быть не более одного идентификатора
     *
     * @return идентификатор ФЛ
     */
    public PersonIdentifier getPersonIdentifier() {
        if (personIdentityList != null && !personIdentityList.isEmpty()) {
            return personIdentityList.get(0);
        }
        return null;
    }
}
