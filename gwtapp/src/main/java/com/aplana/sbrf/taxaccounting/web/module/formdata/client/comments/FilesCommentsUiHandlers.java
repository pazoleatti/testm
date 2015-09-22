package com.aplana.sbrf.taxaccounting.web.module.formdata.client.comments;

import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

/**
 * @author lhaziev
 */
public interface FilesCommentsUiHandlers extends UiHandlers{
    TaxType getTaxType();
    void onSaveClicked(String note, List<FormDataFile> files);
    void onStartLoadFile();
    void onEndLoadFile();
}
