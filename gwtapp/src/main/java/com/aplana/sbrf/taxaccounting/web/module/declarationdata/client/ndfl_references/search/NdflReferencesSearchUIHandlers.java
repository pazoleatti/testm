package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.search;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Set;

public interface NdflReferencesSearchUIHandlers extends UiHandlers {
    void onFindClicked();
    void onClearClicked();
    void onChooseClicked(Set<DataRow<Cell>> selectedItems);
    void onCloseClicked();
}
