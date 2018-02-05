package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationTypeAssignment;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * @author auldanov
 */
public class EditFormsAction extends UnsecuredActionImpl<EditFormResult> implements ActionName {
    private List<DeclarationTypeAssignment> declarationTypeAssignments;
    private List<Integer> performers;
    private boolean isForm = true;

    public List<DeclarationTypeAssignment> getDeclarationTypeAssignments() {
        return declarationTypeAssignments;
    }

    public void setDeclarationTypeAssignments(List<DeclarationTypeAssignment> declarationTypeAssignments) {
        this.declarationTypeAssignments = declarationTypeAssignments;
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
