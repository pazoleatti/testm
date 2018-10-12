package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.identification.DocType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Andrey Drunk
 */
public class PersonDocument extends IdentityObject<Long> {

    /**
     * Ссылка на ФЛ
     */
    private RegistryPerson person;

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

    public static final String TABLE_NAME = "ref_book_id_doc";

    public static final String[] COLUMNS = {"id", "person_id", "doc_id", "doc_number", "inc_rep"};

    /**
     * Список полей бина значения которых передаются в запрос. Порядок соответсвует порядку наименований столбцов в COLUMNS
     */
    public static final String[] FIELDS = {"id", "person.id", "docType.id", "documentNumber", "incRep"};


    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public RegistryPerson getPerson() {
        return person;
    }

    public void setPerson(RegistryPerson person) {
        this.person = person;
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

    public boolean isIncludeReport() {
        return incRep != null && incRep.equals(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonDocument that = (PersonDocument) o;

        if (!docType.equals(that.docType)) return false;
        return documentNumber.replaceAll("[^А-Яа-я\\w]", "").equals(that.documentNumber.replaceAll("[^А-Яа-я\\w]", ""));
    }

    @Override
    public int hashCode() {
        int result = docType.hashCode();
        result = 31 * result + documentNumber.replaceAll("[^А-Яа-я\\w]", "").hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("docType", docType)
                .append("documentNumber", documentNumber)
                .append("incRep", incRep)
                .toString();
    }


}
