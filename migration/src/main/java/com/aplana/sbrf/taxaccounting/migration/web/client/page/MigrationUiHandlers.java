package com.aplana.sbrf.taxaccounting.migration.web.client.page;

import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface MigrationUiHandlers extends UiHandlers {
    void start(List<Long> selectedList);
}
