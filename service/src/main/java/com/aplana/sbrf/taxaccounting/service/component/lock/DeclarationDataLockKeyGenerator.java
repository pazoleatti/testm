package com.aplana.sbrf.taxaccounting.service.component.lock;

import com.aplana.sbrf.taxaccounting.model.OperationType;

/**
 * Интерфейс содержащий логику по генерированию ключей блокировки
 */
public interface DeclarationDataLockKeyGenerator {

    /**
     * Сгенерировать ключ блокировки
     * @param declarationDataId идентификатор налоговой формы
     * @param operationType     тип задачи
     * @return  строку ключа блокировки
     */
    String generateLockKey(Long declarationDataId, OperationType operationType);
}
