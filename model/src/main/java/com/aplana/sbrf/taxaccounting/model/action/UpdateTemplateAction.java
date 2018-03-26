package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateCheck;

import java.util.List;

/**
 * Обертка параметров для операции изменения макета
 */
public class UpdateTemplateAction {

    /**
     * Макет
     */
    private DeclarationTemplate declarationTemplate;

    /**
     * Данные о фатальности проверок
     */
    private List<DeclarationTemplateCheck> checks;

    /**
     * Подтверждено ли предупреждение о наличии форм
     */
    private boolean formsExistWarningConfirmed;

    public DeclarationTemplate getDeclarationTemplate() {
        return declarationTemplate;
    }

    public void setDeclarationTemplate(DeclarationTemplate declarationTemplate) {
        this.declarationTemplate = declarationTemplate;
    }

    public List<DeclarationTemplateCheck> getChecks() {
        return checks;
    }

    public void setChecks(List<DeclarationTemplateCheck> checks) {
        this.checks = checks;
    }

    public boolean isFormsExistWarningConfirmed() {
        return formsExistWarningConfirmed;
    }

    public void setFormsExistWarningConfirmed(boolean formsExistWarningConfirmed) {
        this.formsExistWarningConfirmed = formsExistWarningConfirmed;
    }
}
