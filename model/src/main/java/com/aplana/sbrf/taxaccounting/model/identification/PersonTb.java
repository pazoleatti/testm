package com.aplana.sbrf.taxaccounting.model.identification;

import java.util.Date;

/**
 * Класс содержащий данные о назначенном фзлицу Тербанке
 */
public class PersonTb extends RefBookObject {
    /**
     * Физлицо
     */
    private NaturalPerson naturalPerson;
    /**
     * Ссылка на тербанк назначеннный физлицу
     */
    private int tbDepartmentId;
    /**
     * Время выгрузки данных
     */
    private Date importDate;

    public NaturalPerson getNaturalPerson() {
        return naturalPerson;
    }

    public void setNaturalPerson(NaturalPerson naturalPerson) {
        this.naturalPerson = naturalPerson;
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
