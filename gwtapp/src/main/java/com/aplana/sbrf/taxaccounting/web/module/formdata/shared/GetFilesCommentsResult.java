package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFilesCommentsResult implements Result {

    private boolean readOnlyMode;
    private String uuid;
    private FormData formData;
    private String note;
    private List<FormDataFile> files;

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

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<FormDataFile> getFiles() {
        return files;
    }

    public void setFiles(List<FormDataFile> files) {
        this.files = files;
    }
}
