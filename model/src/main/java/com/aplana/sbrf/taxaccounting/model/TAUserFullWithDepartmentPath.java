package com.aplana.sbrf.taxaccounting.model;

@Deprecated
public class TAUserFullWithDepartmentPath extends TAUserFull {
    private static final long serialVersionUID = 5575964485253262551L;

    String fullDepartmentPath;

    public String getFullDepartmentPath() {
        return fullDepartmentPath;
    }

    public void setFullDepartmentPath(String fullDepartmentPath) {
        this.fullDepartmentPath = fullDepartmentPath;
    }
}
