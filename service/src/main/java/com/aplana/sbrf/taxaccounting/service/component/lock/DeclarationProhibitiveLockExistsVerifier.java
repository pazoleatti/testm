package com.aplana.sbrf.taxaccounting.service.component.lock;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

/**
 * Интерфейс отвечающий за проверку существования блокировок налоговой формы мешающих установки блокировки в зависимости от типа задачи
 */
public interface DeclarationProhibitiveLockExistsVerifier {
    /**
     * Проверка существования блокировок, которые мешают постановке в очередь текущей задачи
     * @param declarationDataId идентификатор налоговой формы
     * @param operationType     операция
     * @param userInfo          информация о пользователе
     * @param logger            логгер
     * @return  объект блокировки в случае успешной установки блокировки иначе null
     */
    LockData verify(Long declarationDataId, OperationType operationType, TAUserInfo userInfo, Logger logger);
}
