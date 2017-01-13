package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

/**
 * @author Lhaziev
 */
public interface DeclarationFilesCommentsUiHandlers extends UiHandlers{
    void onSaveClicked(String note, List<DeclarationDataFile> files, boolean exit);
}
