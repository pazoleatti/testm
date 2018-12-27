package com.aplana.sbrf.taxaccounting.service.component;

import com.aplana.sbrf.taxaccounting.model.LockTaskType;

/**
 * Интерфейс содержащий логику по генерированию ключей блокировки
 */
public interface SimpleDeclarationDataLockKeyGenerator {

    /**
     * Сгенерировать ключ блокировки
     * @param declarationDataId идентификатор налоговой формы
     * @param task  тип задачи
     * @return  строку ключа блокировки
     */
    public String generateLockKey(Long declarationDataId, LockTaskType task);
}
