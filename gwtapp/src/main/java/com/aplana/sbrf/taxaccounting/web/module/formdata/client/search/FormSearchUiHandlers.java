package com.aplana.sbrf.taxaccounting.web.module.formdata.client.search;

import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

/**
 * Created by auldanov on 27.03.2014.
 */
public interface FormSearchUiHandlers extends UiHandlers {
    void open(boolean readOnlyMode, boolean manual, boolean absoluteView, int sessionId);
    void onRangeChange(int start, int count);
    void onClickFoundItem(Long rowIndex);
    void setFormDataId(Long formDataId);
    void setFormTemplateId(Integer formTemplateId);
    void setHiddenColumns(List<Integer> hiddenColumns);
    int getHiddenColumnsCountBefore(Integer columnId);
    void close();
    void clearSearchResults();
}
