package com.aplana.sbrf.taxaccounting.model.action;

/**
 * Обертка параметров для операции ввода/вывода из действия макета
 */
public class UpdateTemplateStatusAction {

    /**
     * Ид макета
     */
    private int templateId;

    /**
     * Подтверждено ли предупреждение о наличии форм
     */
    private boolean formsExistWarningConfirmed;

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public boolean isFormsExistWarningConfirmed() {
        return formsExistWarningConfirmed;
    }

    public void setFormsExistWarningConfirmed(boolean formsExistWarningConfirmed) {
        this.formsExistWarningConfirmed = formsExistWarningConfirmed;
    }
}
