package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class GetTableDataAction extends UnsecuredActionImpl<GetTableDataResult> {

    public GetTableDataAction() {
    }

    private List<Integer> departmentsIds;
    private char taxType;
    private boolean isForm;
    private int startIndex;
    private int count;

    public List<Integer> getDepartmentsIds() {
        return departmentsIds;
    }

    public void setDepartmentsIds(List<Integer> departmentsIds) {
        this.departmentsIds = departmentsIds;
    }

    public char getTaxType() {
        return taxType;
    }

    public void setTaxType(char taxType) {
        this.taxType = taxType;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean form) {
        isForm = form;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
