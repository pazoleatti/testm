package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Stanislav Yasinskiy
 */
public class GetTableDataAction extends UnsecuredActionImpl<GetTableDataResult> implements ActionName {

    private Boolean external;
    private String filter;

    /**
     * @return true - внешние, false - внутренние, null - все
     */
    public Boolean getExternal() {
        return external;
    }


    /**
     * @param external true - внешние, false - внутренние, null - все
     */
    public void setExternal(Boolean external) {
        this.external = external;
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
