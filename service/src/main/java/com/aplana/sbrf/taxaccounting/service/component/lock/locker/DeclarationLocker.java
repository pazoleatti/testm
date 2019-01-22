package com.aplana.sbrf.taxaccounting.service.component.lock.locker;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

/**
 * Интерфейс отвечающий за проверку и установку блокировки
 */
@ScriptExposed
public interface DeclarationLocker {

    /**
     * Устанавливает блокировку на форму, предварительно проверив существование блокировок, которые мешают постановке в очередь текущей задачи
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param operationType     операция
     * @param userInfo          информация о пользователе
     * @param logger            логгер
     * @return объект блокировки в случае успешной установки блокировки иначе null
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

    /**
     * Снимает блокировку выбранного типа с формы.
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param operationType     тип блокировки
     * @param logger            логгер
     */
    void unlock(Long declarationDataId, OperationType operationType, Logger logger);

    /**
     * Проверка наличия блокировки на форме.
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param operationType     тип блокировки
     * @param userInfo          пользователь
     * @return true, если блокировка выбранного типа имеется и установлена данным пользователем
     */
    boolean lockExists(Long declarationDataId, OperationType operationType, TAUserInfo userInfo);
}
