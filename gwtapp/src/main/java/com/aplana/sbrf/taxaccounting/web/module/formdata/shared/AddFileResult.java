package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class AddFileResult implements Result {

    private List<FormDataFile> files;

    public List<FormDataFile> getFiles() {
        return files;
    }

    public void setFile(List<FormDataFile> files) {
        this.files = files;
    }
}
