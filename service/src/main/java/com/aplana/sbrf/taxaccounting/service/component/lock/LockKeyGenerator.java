package com.aplana.sbrf.taxaccounting.service.component.lock;

import com.aplana.sbrf.taxaccounting.model.OperationType;

import java.util.Map;

/**
 * Интерфейс содержащий логику по генерированию ключей блокировки
 */
public interface LockKeyGenerator {

    /**
     * Сгенерировать ключ блокировки
     *
     * @param idTokens      идентификаторы используемые как части ключа
     * @param operationType тип задачи
     * @return строку ключа блокировки
     * @throws IllegalArgumentException если переданная операция не обработана в реализации
     */
    String generateLockKey(Map<String, Long> idTokens, OperationType operationType);
}
