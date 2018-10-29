package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Класс содержащий данные о назначенном фзлицу Тербанке
 */
@Getter @Setter
public class PersonTb extends IdentityObject<Long> {
    /**
     * Физлицо
     */
    private RegistryPerson person;
    /**
     * Тербанк назначеннный физлицу
     */
    private Department tbDepartment;
    /**
     * Время выгрузки данных
     */
    private Date importDate;

    public static final String TABLE_NAME = "ref_book_person_tb";

    public static final String[] COLUMNS = {"id", "person_id", "tb_department_id", "import_date"};

    /**
     * Список полей бина значения которых передаются в запрос. Порядок соответсвует порядку наименований столбцов в COLUMNS
     */
    public static final String[] FIELDS = {"id", "person.id", "tbDepartment.id", "importDate"};

}
