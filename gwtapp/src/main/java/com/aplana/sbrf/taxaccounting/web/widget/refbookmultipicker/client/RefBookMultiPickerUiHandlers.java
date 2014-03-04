package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.PickerState;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Collection;
import java.util.Date;

public interface RefBookMultiPickerUiHandlers extends UiHandlers {

    void init(PickerState pickerState);

    void reload(Date relevanceDate);

    void rangeChanged(int startIndex, int max);

    void loadingForSelection(Collection<Long> ids);

    void onSort(Integer columnIndex, boolean isSortAscending);

    void find(String searchPattern);
}
