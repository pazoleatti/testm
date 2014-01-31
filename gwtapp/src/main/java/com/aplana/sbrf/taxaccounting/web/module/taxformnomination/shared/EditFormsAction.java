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
    private Integer performer;

    public List<FormTypeKind> getFormTypeKinds() {
        return formTypeKinds;
    }

    public void setFormTypeKinds(List<FormTypeKind> formTypeKinds) {
        this.formTypeKinds = formTypeKinds;
    }

    public Integer getPerformer() {
        return performer;
    }

    public void setPerformer(Integer performer) {
        this.performer = performer;
    }

    @Override
    public String getName() {
        return "Редактирование исполнителей";
    }
}
