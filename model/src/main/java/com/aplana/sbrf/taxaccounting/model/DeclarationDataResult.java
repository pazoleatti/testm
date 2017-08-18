package com.aplana.sbrf.taxaccounting.model;

/**
 * Моджельный класс для передачи данных связанных с налоговыми формами
 */
public class DeclarationDataResult {
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
