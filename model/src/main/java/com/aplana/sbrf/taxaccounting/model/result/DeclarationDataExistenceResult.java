package com.aplana.sbrf.taxaccounting.model.result;

public class DeclarationDataExistenceResult {
    private boolean exists;

    public DeclarationDataExistenceResult(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() {
        return exists;
    }
}
