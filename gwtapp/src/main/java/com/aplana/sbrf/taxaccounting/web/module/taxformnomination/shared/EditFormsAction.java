package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * @author auldanov
 */
public class EditFormsAction extends UnsecuredActionImpl<EditFormResult> implements ActionName {
    private List<FormTypeKind> formTypeKinds;
    private List<Integer> performers;
    private boolean isForm = true;

    public List<FormTypeKind> getFormTypeKinds() {
        return formTypeKinds;
    }

    public void setFormTypeKinds(List<FormTypeKind> formTypeKinds) {
        this.formTypeKinds = formTypeKinds;
    }

    public List<Integer> getPerformers() {
        return performers;
    }

    public void setPerformers(List<Integer> performers) {
        this.performers = performers;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean isForm) {
        this.isForm = isForm;
    }

    @Override
    public String getName() {
        return "Редактирование исполнителей";
    }
}
