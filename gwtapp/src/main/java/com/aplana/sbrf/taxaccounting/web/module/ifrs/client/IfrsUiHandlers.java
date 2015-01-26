package com.aplana.sbrf.taxaccounting.web.module.ifrs.client;

import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;
import java.util.Set;

/**
 * Created by lhaziev on 22.10.2014.
 */
public interface IfrsUiHandlers extends UiHandlers {
    void reloadTable();
    void onClickCreate();
    void onClickCalc(Integer id);
    void updateStatus(List<IfrsRow> records);
    void onDeleteClicked(Set<IfrsRow> records);
}
