package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Andrey Drunk
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class IdDoc extends IdentityObject<Long> {

    /**
     * Ссылка на ФЛ
     */
    private RegistryPerson person;

    /**
     * Тип документа
     */
    private RefBookDocType docType;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdDoc that = (IdDoc) o;

        if (!docType.equals(that.docType)) return false;
        return documentNumber.replaceAll("[^А-Яа-я\\w]", "").equals(that.documentNumber.replaceAll("[^А-Яа-я\\w]", ""));
    }

    @Override
    public int hashCode() {
        int result = docType.hashCode();
        result = 31 * result + documentNumber.replaceAll("[^А-Яа-я\\w]", "").hashCode();
        return result;
    }
}
