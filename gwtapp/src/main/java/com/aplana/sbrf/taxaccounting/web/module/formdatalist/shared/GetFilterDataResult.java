package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFilterDataResult implements Result {

    public GetFilterDataResult(){

    }

    private List<Department> departments;
    private List<FormType> kinds;

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<FormType> getKinds() {
        return kinds;
    }

    public void setKinds(List<FormType> kinds) {
        this.kinds = kinds;
    }

}
