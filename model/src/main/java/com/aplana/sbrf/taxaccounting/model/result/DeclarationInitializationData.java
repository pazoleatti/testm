package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Data;

@Data
public class DeclarationInitializationData extends DeclarationDataResult {
    private long declarationKindId;
    private int declarationTypeId;

    public DeclarationInitializationData(boolean existsDeclarationData) {
        setExistDeclarationData(existsDeclarationData);
    }

    public DeclarationInitializationData(boolean existsDeclarationData, long declarationKindId, int declarationTypeId) {
        this.declarationKindId = declarationKindId;
        this.declarationTypeId = declarationTypeId;
        setExistDeclarationData(existsDeclarationData);
    }
}
