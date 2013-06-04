package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 * Date: 2013
 * Запрос для получение отфильтрованного журнала аудита
 * Данные для фильтра в жураале аудита
 */
public class GetAuditFilterDataAction extends UnsecuredActionImpl<GetAuditFilterDataResult> implements ActionName{

    @Override
    public String getName() {
        return "Получение журнал аудита";
    }
}
