package com.aplana.sbrf.taxaccounting.service.component.lock.locker;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.List;

/**
 * Интерфейс отвечающий за проверку и установку блокировки
 */
public interface DeclarationLocker {

    /**
     * Устанавливает блокировку на форму, предварительно проверив существование блокировок, которые мешают постановке в очередь текущей задачи
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param operationType     операция
     * @param userInfo          информация о пользователе
     * @param logger            логгер
     * @return список объектов блокировок в случае успешной установки блокировки иначе null
     */
    LockData establishLock(Long declarationDataId, OperationType operationType, TAUserInfo userInfo, Logger logger);

    /**
     * Устанавливает блокировки на формы, предварительно проверив существование блокировок, которые мешают постановке в очередь текущей задачи
     *
     * @param declarationDataIdList идентификатор налоговой формы
     * @param operationType         операция
     * @param userInfo              информация о пользователе
     * @param logger                логгер
     * @return список объектов блокировок в случае успешной установки блокировки иначе null
     */
    List<LockData> establishLock(List<Long> declarationDataIdList, OperationType operationType, TAUserInfo userInfo, Logger logger);
}
