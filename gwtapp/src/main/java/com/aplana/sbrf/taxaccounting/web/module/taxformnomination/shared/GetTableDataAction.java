package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.aplana.sbrf.taxaccounting.model.TaxNominationColumnEnum;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class GetTableDataAction extends UnsecuredActionImpl<GetTableDataResult> {

    public GetTableDataAction() {
    }

    private List<Integer> departmentsIds;
    private char taxType;
    private boolean isForm = true;
    private int startIndex;
    private int count;
    private TaxNominationColumnEnum sortColumn = TaxNominationColumnEnum.DEPARTMENT_FULL_NAME;
    private boolean asc = true;

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

    public void setSortColumn(TaxNominationColumnEnum sortColumn) {
        this.sortColumn = sortColumn;
    }

    public TaxNominationColumnEnum getSortColumn() {
        return sortColumn;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public boolean isAsc() {
        return asc;
    }
}
