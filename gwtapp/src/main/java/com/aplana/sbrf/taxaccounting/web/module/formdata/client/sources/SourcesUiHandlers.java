package com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * @author auldanov
 */
public interface SourcesUiHandlers extends UiHandlers{
    void open();

    void onRangeChange(int start);
}
