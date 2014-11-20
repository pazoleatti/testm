package com.aplana.sbrf.taxaccounting.web.module.ifrs.client;

import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

/**
 * Created by lhaziev on 22.10.2014.
 */
public interface IfrsUiHandlers extends UiHandlers {
    void reloadTable();
    void onClickCreate();
    void onClickCalc(Integer id);
    void updateStatus(List<IfrsRow> records);
}
