package com.aplana.sbrf.taxaccounting.web.module.formdata.client.comments;

import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

/**
 * @author Lhaziev
 */
public interface FilesCommentsUiHandlers extends UiHandlers{
    void onSaveClicked(String note, List<FormDataFile> files, boolean exit);
}
