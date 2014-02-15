package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;
import java.util.List;

public interface RefBookMultiPickerUiHandlers extends UiHandlers {

    void init(long refBookAttrId, String filter, Date relevanceDate);

    void reload(Date relevanceDate);

    void rangeChanged(int startIndex, int max);

    void loadingForSelection(List<Long> id);

    void onSort(Integer columnIndex, boolean isSortAscending);

    void search();

    void versionChange();
}
