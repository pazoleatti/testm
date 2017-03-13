package com.aplana.sbrf.taxaccounting.model.identification;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Идентификатор ФЛ
 *
 * @author Andrey Drunk
 */
public class PersonIdentifier extends RefBookObject {

    /**
     * Ссылка на ФЛ
     */
    private NaturalPerson naturalPerson;

    /**
     * Идентификатор АСНУ из справочника
     */
    private Long asnuId;

    /**
     * ИНП
     */
    private String inp;


    public NaturalPerson getNaturalPerson() {
        return naturalPerson;
    }

    public void setNaturalPerson(NaturalPerson naturalPerson) {
        this.naturalPerson = naturalPerson;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
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
