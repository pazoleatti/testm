package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

/**
 * Данные для заполнения модального окна
 */
public class GetDestanationPopupDataResult implements Result {
    // Список всех подразделений
    private List<Department> departments;

    // Список id подразделений, доступных пользователю
    private Set<Integer> availableDepartments;

    private List<Department> performers;

    private Set<Integer> availablePerformers;

    public List<Department> getPerformers() {
        return performers;
    }

    public void setPerformers(List<Department> performers) {
        this.performers = performers;
    }

    public Set<Integer> getAvailablePerformers() {
        return availablePerformers;
    }

    public void setAvailablePerformers(Set<Integer> availablePerformers) {
        this.availablePerformers = availablePerformers;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public Set<Integer> getAvailableDepartments() {
        return availableDepartments;
    }

    public void setAvailableDepartments(Set<Integer> availableDepartments) {
        this.availableDepartments = availableDepartments;
    }
}
