package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class AddFileResult implements Result {

    private FormDataFile file;

    public FormDataFile getFile() {
        return file;
    }

    public void setFile(FormDataFile file) {
        this.file = file;
    }
}
