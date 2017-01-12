package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class AddDeclarationFileResult implements Result {

    private List<DeclarationDataFile> files;

    public List<DeclarationDataFile> getFiles() {
        return files;
    }

    public void setFiles(List<DeclarationDataFile> files) {
        this.files = files;
    }
}
