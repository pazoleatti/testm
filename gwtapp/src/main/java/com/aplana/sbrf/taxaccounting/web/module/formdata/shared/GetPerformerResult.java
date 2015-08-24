package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

public class GetPerformerResult implements Result {
    private List<Department> departments;
    private Set<Integer> availableDepartments;
    private boolean readOnlyMode;
    private boolean createLock = false;
    private String uuid;
    private FormData formData;

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

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    public boolean isCreateLock() {
        return createLock;
    }

    public void setCreateLock(boolean creteLock) {
        this.createLock = creteLock;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}
