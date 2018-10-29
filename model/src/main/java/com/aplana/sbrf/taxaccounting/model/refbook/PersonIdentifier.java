package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Идентификатор ФЛ
 *
 * @author Andrey Drunk
 */
@Getter @Setter
@ToString
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
     * ИНП
     */
    private String inp;

    public static final String TABLE_NAME = "ref_book_id_tax_payer";

    public static final String[] COLUMNS = {"id", "person_id", "inp", "as_nu"};

    /**
     * Список полей бина значения которых передаются в запрос. Порядок соответсвует порядку наименований столбцов в COLUMNS
     */
    public static final String[] FIELDS = {"id", "person.id", "inp", "asnu.id"};

}
