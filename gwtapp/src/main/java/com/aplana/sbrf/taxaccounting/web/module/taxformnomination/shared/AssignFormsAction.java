package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Действие Назначение форм подразделениям
 * @author auldanov
 */
public class AssignFormsAction extends UnsecuredActionImpl<AssignFormsResult> implements ActionName {

    private List<Integer> departments;
    private FormDataKind formDataKind;
    private List<Integer> formTypes;
    private Integer performer;

    public List<Integer> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Integer> departments) {
        this.departments = departments;
    }

    public FormDataKind getFormDataKind() {
        return formDataKind;
    }

    public void setFormDataKind(FormDataKind formDataKind) {
        this.formDataKind = formDataKind;
    }

    public List<Integer> getFormTypes() {
        return formTypes;
    }

    public void setFormTypes(List<Integer> formTypes) {
        this.formTypes = formTypes;
    }

    public Integer getPerformer() {
        return performer;
    }

    public void setPerformer(Integer performer) {
        this.performer = performer;
    }

    @Override
    public String getName() {
        return "Назначение форм подразделениям";
    }
}
