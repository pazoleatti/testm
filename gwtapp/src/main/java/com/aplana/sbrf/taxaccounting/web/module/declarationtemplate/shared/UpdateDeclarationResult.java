package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

public class UpdateDeclarationResult implements Result {
    private int declarationTemplateId;
    private String logUuid;
    private boolean isConfirmNeeded;

    public boolean isConfirmNeeded() {
        return isConfirmNeeded;
    }

    public void setConfirmNeeded(boolean isConfirmNeeded) {
        this.isConfirmNeeded = isConfirmNeeded;
    }

    public String getLogUuid() {
        return logUuid;
    }

    public void setLogUuid(String logUuid) {
        this.logUuid = logUuid;
    }

    public int getDeclarationTemplateId() {
        return declarationTemplateId;
    }

    public void setDeclarationTemplateId(int declarationTemplateId) {
        this.declarationTemplateId = declarationTemplateId;
    }
}
