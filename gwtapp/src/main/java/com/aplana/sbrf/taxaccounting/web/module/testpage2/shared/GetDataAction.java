package com.aplana.sbrf.taxaccounting.web.module.testpage2.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDataAction extends UnsecuredActionImpl<GetDataResult> implements ActionName {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return "Получение тестовых данных";
    }
}
