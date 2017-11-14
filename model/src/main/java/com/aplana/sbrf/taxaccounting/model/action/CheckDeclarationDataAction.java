package com.aplana.sbrf.taxaccounting.model.action;

public class CheckDeclarationDataAction {

    /**
     * если true, то удаляем старую задачу(и оправляем оповещения подписавщимся пользователям), иначе, если задача уже запущена, вызываем диалог
     */
    private boolean force;

    private long declarationId;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public long getDeclarationId() {
        return declarationId;
    }

    public void setDeclarationId(long declarationId) {
        this.declarationId = declarationId;
    }
}
