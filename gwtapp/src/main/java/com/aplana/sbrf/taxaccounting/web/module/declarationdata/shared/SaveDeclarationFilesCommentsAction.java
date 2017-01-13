package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Сохранение данных для формы "Файлы и комментарии"
 * @author lhaziev
 */
public class SaveDeclarationFilesCommentsAction extends UnsecuredActionImpl<GetDeclarationFilesCommentsResult> {
    private DeclarationData declarationData;
    private List<DeclarationDataFile> files;
    private String note;

    public DeclarationData getDeclarationData() {
        return declarationData;
    }

    public void setDeclarationData(DeclarationData declarationData) {
        this.declarationData = declarationData;
    }

    public List<DeclarationDataFile> getFiles() {
        return files;
    }

    public void setFiles(List<DeclarationDataFile> files) {
        this.files = files;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
