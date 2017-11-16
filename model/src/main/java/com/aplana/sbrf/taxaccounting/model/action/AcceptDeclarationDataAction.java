package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.TaxType;

public class AcceptDeclarationDataAction {
    private long declarationId;
    private TaxType taxType;
    /**
     * если true, то удаляем старую задачу(и оправляем оповещения подписавщимся пользователям), иначе, если задача уже запущена, вызываем диалог
     */
    private boolean force;
    /**
     * если true, то удаляем задачи, которые должны удаляться при запуске текущей, иначе, если есть такие задачи, вызываем диалог
     */
    private boolean cancelTask;

    public long getDeclarationId() {
        return declarationId;
    }

    public void setDeclarationId(long declarationId) {
        this.declarationId = declarationId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isCancelTask() {
        return cancelTask;
    }

    public void setCancelTask(boolean cancelTask) {
        this.cancelTask = cancelTask;
    }

}