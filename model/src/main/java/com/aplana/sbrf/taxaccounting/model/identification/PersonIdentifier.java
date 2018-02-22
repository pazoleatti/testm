package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
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
