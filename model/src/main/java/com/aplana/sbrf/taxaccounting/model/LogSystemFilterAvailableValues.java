package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * User: avanteev
 * Модельный класс, содержащий информацию о значениях, которые допустимы
 * в фильтре по журналу аудита.
 */
public class LogSystemFilterAvailableValues implements Serializable {

    /**
     * Набор подразделений
     * В случае, если у пользователя есть доступ ко всем подразделениям, нужно присваивать null
     */
    private List<Department> departments;
    /**
     * Список налоговых период.
     */
    private List<TaxPeriod> taxPeriods;

    /**
     * Набор видов налоговых форм
     */
    private List<FormType> formTypes;
    /**
     * Набор типов налоговых форм
     */
    private List<FormDataKind> kinds;

    /**
     * Список всех пользователей
     */
    private List<TAUser> users;

    /**
     * Типы налоговых форм
     */
    private List<TaxType> taxTypes;

    public List<TaxType> getTaxTypes() {
        return taxTypes;
    }

    public void setTaxTypes(List<TaxType> taxTypes) {
        this.taxTypes = taxTypes;
    }

    public List<TAUser> getUsers() {
        return users;
    }

    public void setUsers(List<TAUser> users) {
        this.users = users;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<TaxPeriod> getTaxPeriods() {
        return taxPeriods;
    }

    public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
        this.taxPeriods = taxPeriods;
    }

    public List<FormType> getFormTypes() {
        return formTypes;
    }

    public void setFormTypes(List<FormType> formTypes) {
        this.formTypes = formTypes;
    }

    public List<FormDataKind> getKinds() {
        return kinds;
    }

    public void setKinds(List<FormDataKind> kinds) {
        this.kinds = kinds;
    }
}
