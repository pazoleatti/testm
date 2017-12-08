package com.aplana.sbrf.taxaccounting.model.result;

public class DeclarationDataExistenceAndKindResult {
    private boolean exists;
    private long declarationKindId;

    public DeclarationDataExistenceAndKindResult(boolean exists) {
        this.exists = exists;
    }

    public DeclarationDataExistenceAndKindResult(boolean exists, long declarationKindId) {
        this.exists = exists;
        this.declarationKindId = declarationKindId;
    }

    public boolean isExists() {
        return exists;
    }

    public long getDeclarationKindId() {
        return declarationKindId;
    }
}
