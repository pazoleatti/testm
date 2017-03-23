package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class DeclarationDataResult implements Result {
    private boolean existDeclarationData = true;
    private long declarationDataId;

    public boolean isExistDeclarationData() {
        return existDeclarationData;
    }

    public void setExistDeclarationData(boolean existDeclarationData) {
        this.existDeclarationData = existDeclarationData;
    }

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }
}
