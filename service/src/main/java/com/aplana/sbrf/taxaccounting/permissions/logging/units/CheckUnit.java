package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

/**
 * Используется {@link com.aplana.sbrf.taxaccounting.permissions.logging.LoggingPermissionChecker}. Каждая реализация
 * интерфейса является отдельным звеном проверки прав доступа для налоговой формы.
 */
public interface CheckUnit {

    /**
     * Отдельная проверка прав доступа и создание сообщение в логгере по этой проверке.
     * @param logger            логгер
     * @param userInfo          информация о пользователе
     * @param declarationData   объект налоговой формы
     * @param operationName     строковое наименование операции для вывода сообщения
     * @return возвращает <code>true</code> - если проверка пройдена, возвращает <code>false</code> если проверка не
     * пройдена
     */
    boolean check(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, String operationName);

}
