package com.aplana.sbrf.taxaccounting.web.module.scriptexecution.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CheckRoleAction extends UnsecuredActionImpl<CheckRoleResult> implements ActionName {
    @Override
    public String getName() {
        return "Проверить роль пользователя";
    }
}
