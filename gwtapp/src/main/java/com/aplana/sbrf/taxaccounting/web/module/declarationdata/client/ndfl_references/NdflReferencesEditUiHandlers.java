package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;
import java.util.Set;

public interface NdflReferencesEditUiHandlers extends UiHandlers {
    void onAddClicked();
    void onRemoveClicked(Set<DataRow<Cell>> selectedItems);
    void onSaveClicked(String note, List<DataRow<Cell>> files, boolean exit);
    void onCancelClicked();
}
