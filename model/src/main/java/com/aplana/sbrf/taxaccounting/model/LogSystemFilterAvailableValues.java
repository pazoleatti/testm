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
     * Набор видов налоговых форм
     */
    private List<FormType> formTypes;

    /**
     * Список всех пользователей
     */
    private List<TAUser> users;

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

    public List<FormType> getFormTypes() {
        return formTypes;
    }

    public void setFormTypes(List<FormType> formTypes) {
        this.formTypes = formTypes;
    }
}
