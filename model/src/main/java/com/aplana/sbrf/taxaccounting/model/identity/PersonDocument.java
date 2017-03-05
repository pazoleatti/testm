package com.aplana.sbrf.taxaccounting.model.identity;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Andrey Drunk
 */
public class PersonDocument extends IdentityObject<Long> {

    /**
     * Ссылка на ФЛ
     */
    private NaturalPerson naturalPerson;

    /**
     * Тип документа
     */
    private DocType docType;

    /**
     * Номер документа
     */
    private String documentNumber;

    /**
     * Включается в отчетность
     */
    private Integer incRep;


    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public NaturalPerson getNaturalPerson() {
        return naturalPerson;
    }

    public void setNaturalPerson(NaturalPerson naturalPerson) {
        this.naturalPerson = naturalPerson;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getIncRep() {
        return incRep;
    }

    public void setIncRep(Integer incRep) {
        this.incRep = incRep;
    }

    public boolean isIncludeReport(){
        return incRep != null && incRep.equals(1);
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("docType", docType)
                .append("documentNumber", documentNumber)
                .append("incRep", incRep)
                .toString();
    }
}
