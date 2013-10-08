package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Stanislav Yasinskiy
 */
public class GetTableDataAction extends UnsecuredActionImpl<GetTableDataResult> implements ActionName {

    private RefBookType refBookType;
    private String filter;

    public RefBookType getType() {
        return refBookType;
    }

    public void setType(RefBookType refBookType) {
        this.refBookType = refBookType;
    }

    @Override
    public String getName() {
        return "Получение начальных данных";
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
