package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * User: avanteev
 * Date: 2013
 */
public class TAUserFull implements Serializable {

    private TAUser user;
    private Department department;

    public TAUser getUser() {
        return user;
    }

    public void setUser(TAUser user) {
        this.user = user;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
