package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.Date;

/**
 * Класс содержащий данные о назначенном фзлицу Тербанке
 */
public class PersonTb extends IdentityObject<Long> {
    /**
     * Физлицо
     */
    private RegistryPerson person;
    /**
     * Ссылка на тербанк назначеннный физлицу
     */
    private int tbDepartmentId;
    /**
     * Время выгрузки данных
     */
    private Date importDate;

    public static final String TABLE_NAME = "ref_book_person_tb";

    public static final String[] COLUMNS = {"id", "person_id", "tb_department_id", "import_date"};

    /**
     * Список полей бина значения которых передаются в запрос. Порядок соответсвует порядку наименований столбцов в COLUMNS
     */
    public static final String[] FIELDS = {"id", "person.id", "tbDepartmentId", "importDate"};

    public RegistryPerson getPerson() {
        return person;
    }

    public void setPerson(RegistryPerson person) {
        this.person = person;
    }

    public int getTbDepartmentId() {
        return tbDepartmentId;
    }

    public void setTbDepartmentId(int tbDepartmentId) {
        this.tbDepartmentId = tbDepartmentId;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }
}
