package com.aplana.sbrf.taxaccounting.web.module.formdata.client.search;

import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

/**
 * Created by auldanov on 27.03.2014.
 */
public interface FormSearchUiHandlers extends UiHandlers {
    void open();
    void onRangeChange(int start, int count);
    void onClickFoundItem(Long rowIndex);
    public void setFormDataId(Long formDataId);
    public void setHiddenColumns(List<Integer> hiddenColumns);
    public int getHiddenColumnsCountBefore(Integer columnId);
    void close();
}
