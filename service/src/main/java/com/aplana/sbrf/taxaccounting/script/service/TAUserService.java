package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

@ScriptExposed
public interface TAUserService {
    /**
     * Возвращает информацию о текущем пользователе системы
     */
    TAUserInfo getCurrentUserInfo();
}
