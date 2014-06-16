package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.model.TaxNominationColumnEnum;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Сравниватель для данных представления "Назначения НФ и деклараций"
 *
 * @author aivanov
 * @since 16.06.2014
 */
public class TaxNominationDataComparator implements Comparator<FormTypeKind> {

    TaxNominationColumnEnum sort = TaxNominationColumnEnum.DEPARTMENT;
    boolean asc = true;
    boolean isForm = true;
    Map<Integer, String> departmentFullNames = new HashMap<Integer, String>();

    public int compare(FormTypeKind o1, FormTypeKind o2) {
        int i = asc ? 1 : -1;
        if (isForm) {
            return i * compareForm(o1, o2);
        } else {
            return i * compareDeclaration(o1, o2);
        }
    }

    private int compareForm(FormTypeKind o1, FormTypeKind o2){
        int result = 0;
        switch (sort) {
            case DEC_TYPE:
            case DEPARTMENT:
                String depFullName1 = departmentFullNames.get(o1.getDepartment().getId());
                String depFullName2 = departmentFullNames.get(o2.getDepartment().getId());
                result = depFullName1.compareTo(depFullName2);
                if (result == 0) {
                    result = Integer.valueOf(o1.getKind().getId()).compareTo(o2.getKind().getId());
                    if (result == 0) {
                        result = o1.getName().compareTo(o2.getName());
                    }
                }
                break;
            case FORM_TYPE:
                result = o1.getName().compareTo(o2.getKind().getName());
                if (result == 0) {
                    result = Integer.valueOf(o1.getKind().getId()).compareTo(o2.getKind().getId());
                }
                break;
            case FORM_KIND:
                result = Integer.valueOf(o1.getKind().getId()).compareTo(o2.getKind().getId());
                break;
            case PERFORMER:
                String repFullName1 = o1.getPerformer()!= null ? departmentFullNames.get(o1.getPerformer().getId()) : "";
                String repFullName2 = o2.getPerformer()!= null ? departmentFullNames.get(o2.getPerformer().getId()) : "";
                result = repFullName1.compareTo(repFullName2);
                break;
        }

        return result;
    }

    private int compareDeclaration(FormTypeKind o1, FormTypeKind o2){
        int result = 0;
        switch (sort) {
            case DEC_TYPE:
                result = o1.getName().compareTo(o2.getName());
                break;
            case FORM_TYPE:
            case FORM_KIND:
            case PERFORMER:
            case DEPARTMENT:
                String depFullName1 = departmentFullNames.get(o1.getDepartment().getId());
                String depFullName2 = departmentFullNames.get(o2.getDepartment().getId());
                result = depFullName1.compareTo(depFullName2);
                if (result == 0) {
                    result = o1.getName().compareTo(o2.getName());
                }
                break;
        }

        return result;
    }

    public void setup(TaxNominationColumnEnum sort, boolean asc, boolean isForm, Map<Integer, String> departmentFullNames) {
        this.sort = sort;
        this.asc = asc;
        this.isForm = isForm;
        this.departmentFullNames = departmentFullNames;
    }

    public TaxNominationColumnEnum getSort() {
        return sort;
    }

    public void setSort(TaxNominationColumnEnum sort) {
        this.sort = sort;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean isForm) {
        this.isForm = isForm;
    }

    public Map<Integer, String> getDepartmentFullNames() {
        return departmentFullNames;
    }

    public void setDepartmentFullNames(Map<Integer, String> departmentFullNames) {
        this.departmentFullNames = departmentFullNames;
    }
}
