package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;

public class GetDeclarationResult implements Result {
    private DeclarationTemplate declarationTemplate;
    private Date endDate;
    private boolean lockedByAnotherUser = false;
    private String uuid;

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public DeclarationTemplate getDeclarationTemplate() {
        return declarationTemplate;
    }

    public void setDeclarationTemplate(DeclarationTemplate declaration) {
        this.declarationTemplate = declaration;
    }

    public boolean isLockedByAnotherUser() {
        return lockedByAnotherUser;
    }

    public void setLockedByAnotherUser(boolean lockedByAnotherUser) {
        this.lockedByAnotherUser = lockedByAnotherUser;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
