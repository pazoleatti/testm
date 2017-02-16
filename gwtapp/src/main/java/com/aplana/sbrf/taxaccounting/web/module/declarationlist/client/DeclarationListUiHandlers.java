package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;


import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface DeclarationListUiHandlers extends UiHandlers {

    void onRangeChange(int start, int length);
    void onCreateClicked();
    void onCreateReportsClicked();
    void onDownloadReportsClicked();
    List<Long> getSelectedItemIds();
    void onRecalculateClicked();
    void accept(boolean accepted);
    void delete();
    void check();

}
