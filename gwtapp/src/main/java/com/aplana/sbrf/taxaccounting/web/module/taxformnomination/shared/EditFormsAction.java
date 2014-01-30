package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * @author auldanov
 */
public class EditFormsAction extends UnsecuredActionImpl<EditFormResult> implements ActionName {
    private List<Integer> departments;
    private FormDataKind formDataKind;
    private Integer formTypeId;
    private List<Integer> performers;

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

    public Integer getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public List<Integer> getPerformers() {
        return performers;
    }

    public void setPerformers(List<Integer> performers) {
        this.performers = performers;
    }

    @Override
    public String getName() {
        return "Редактирование исполнителей";
    }
}
