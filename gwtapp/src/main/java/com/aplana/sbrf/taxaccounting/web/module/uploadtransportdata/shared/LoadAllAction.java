package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class LoadAllAction extends UnsecuredActionImpl<LoadAllResult> implements ActionName {
    @Override
    public String getName() {
        return "Загрука ТФ из каталога";
    }
}
