package com.aplana.sbrf.taxaccounting.service.component.lock.descriptor;

import com.aplana.sbrf.taxaccounting.model.OperationType;

/**
 * Интерфейс содержающий логику по созданию описания блокировки для НФ
 */
public interface DeclarationDataKeyLockDescriptor {

    /**
     * Создать описание блокировки для НФ в зависимости от типа задачи
     * @param declarationDataId идентификатор НФ
     * @param operationType     тип задачи
     * @return  строку описания
     */
    String createKeyLockDescription(Long declarationDataId, OperationType operationType);
}
