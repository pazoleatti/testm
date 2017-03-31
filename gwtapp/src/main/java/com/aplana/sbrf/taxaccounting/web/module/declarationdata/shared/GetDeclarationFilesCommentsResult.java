package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;

import java.util.List;

public class GetDeclarationFilesCommentsResult extends DeclarationDataResult {

    private boolean readOnlyMode;
    private String uuid;
    private DeclarationData declarationData;
    private String note;
    private List<DeclarationDataFile> files;

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public DeclarationData getDeclarationData() {
        return declarationData;
    }

    public void setDeclarationData(DeclarationData declarationData) {
        this.declarationData = declarationData;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<DeclarationDataFile> getFiles() {
        return files;
    }

    public void setFiles(List<DeclarationDataFile> files) {
        this.files = files;
    }
}
