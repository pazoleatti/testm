package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Stanislav Yasinskiy
 */
public class GetTableDataAction extends UnsecuredActionImpl<GetTableDataResult> implements ActionName {

    private Type type;
    private String filter;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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
