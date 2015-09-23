package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Сохранение данных для формы "Файлы и комментарии"
 * @author lhaziev
 */
public class SaveFilesCommentsAction extends UnsecuredActionImpl<GetFilesCommentsResult> {
    private FormData formData;
    private List<FormDataFile> files;
    private String note;

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    public List<FormDataFile> getFiles() {
        return files;
    }

    public void setFiles(List<FormDataFile> files) {
        this.files = files;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
