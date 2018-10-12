package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Идентификатор ФЛ
 *
 * @author Andrey Drunk
 */
public class PersonIdentifier extends IdentityObject<Long> {

    /**
     * Ссылка на ФЛ
     */
    private RegistryPerson person;

    /**
     * АСНУ
     */
    private RefBookAsnu asnu;

    /**
     * Идентификатор АСНУ из справочника
     * @deprecated см {@link #asnu}
     */
    @Deprecated
    private Long asnuId;

    /**
     * ИНП
     */
    private String inp;

    public static final String TABLE_NAME = "ref_book_id_tax_payer";

    public static final String[] COLUMNS = {"id", "person_id", "inp", "as_nu"};

    /**
     * Список полей бина значения которых передаются в запрос. Порядок соответсвует порядку наименований столбцов в COLUMNS
     */
    public static final String[] FIELDS = {"id", "person.id", "inp", "asnu.id"};

    public RegistryPerson getPerson() {
        return person;
    }

    public void setPerson(RegistryPerson person) {
        this.person = person;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public RefBookAsnu getAsnu() {
        return asnu;
    }

    public void setAsnu(RefBookAsnu asnu) {
        this.asnu = asnu;
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("asnuId", asnuId)
                .append("inp", inp)
                .toString();
    }
}
