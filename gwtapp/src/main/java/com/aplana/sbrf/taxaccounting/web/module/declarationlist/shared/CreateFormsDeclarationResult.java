package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreateFormsDeclarationResult implements Result {
	private static final long serialVersionUID = -1905610484196633148L;

    private String uuid;
    private String restartMsg;
    /**
     * true - создана новая задача
     * false - есть блокировка
     */
    private boolean status;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRestartMsg() {
        return restartMsg;
    }

    public void setRestartMsg(String restartMsg) {
        this.restartMsg = restartMsg;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
