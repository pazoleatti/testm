package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Andrey Drunk
 */
@Getter @Setter
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
