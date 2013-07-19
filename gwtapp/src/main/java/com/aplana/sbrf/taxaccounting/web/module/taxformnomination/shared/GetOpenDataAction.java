package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *   Action формы настроек подразделений
 *   @author Stanislav Yasinskiy
 */
public class GetOpenDataAction extends UnsecuredActionImpl<GetOpenDataResult> implements ActionName {
    @Override
    public String getName() {
        return "Получение начальных данных";
    }
}
