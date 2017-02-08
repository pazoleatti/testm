package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

/**
 * @author Lhaziev
 */
public interface DeclarationFilesCommentsUiHandlers extends UiHandlers{
    void onSaveClicked(String note, List<DataRow<Cell>> files, boolean exit);
    List<DataRow<Cell>> copyFiles(List<DataRow<Cell>> from);
}
