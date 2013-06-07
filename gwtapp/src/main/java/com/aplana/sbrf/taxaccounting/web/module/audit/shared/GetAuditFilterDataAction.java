package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 * Date: 2013
 * Получение данных для заполнения фильтра
 */
public class GetAuditFilterDataAction extends UnsecuredActionImpl<GetAuditFilterDataResult> implements ActionName{

    @Override
    public String getName() {
        return "Получение данных фильтра";
    }
}
