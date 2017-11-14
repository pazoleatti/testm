package com.aplana.sbrf.taxaccounting.model.action;

public class MoveToCreateAction {

    private Long declarationDataId;

    private String reason;

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
