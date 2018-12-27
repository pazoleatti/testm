package com.aplana.sbrf.taxaccounting.service.component;

import com.aplana.sbrf.taxaccounting.model.LockTaskType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

/**
 * Интерфейс отвечающий за проверку существования блокировок налоговой формы мешающих установки блокировки в зависимости от типа задачи
 */
public interface DeclarationProhibitiveLockExistsVerifier {
    /**
     * Проверка существования блокировок, которые мешают постановке в очередь текущей задачи
     * @param declarationDataId идентификатор налоговой формы
     * @param logger            логгер
     * @return  true, если проверка пройдена
     */
    boolean verify(Long declarationDataId, LockTaskType task, Logger logger);
}
