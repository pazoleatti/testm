package com.aplana.sbrf.taxaccounting.model;

import java.util.List;

public class DeclarationDataFileComment {

    private long declarationId;
    private List<DeclarationDataFile> declarationDataFiles;
    private String comment;

    public DeclarationDataFileComment() {
    }

    public List<DeclarationDataFile> getDeclarationDataFiles() {
        return declarationDataFiles;
    }

    public void setDeclarationDataFiles(List<DeclarationDataFile> declarationDataFiles) {
        this.declarationDataFiles = declarationDataFiles;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getDeclarationId() {
        return declarationId;
    }

    public void setDeclarationId(long declarationId) {
        this.declarationId = declarationId;
    }
}
