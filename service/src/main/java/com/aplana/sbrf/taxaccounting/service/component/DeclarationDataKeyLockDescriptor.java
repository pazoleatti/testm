package com.aplana.sbrf.taxaccounting.service.component;

import com.aplana.sbrf.taxaccounting.model.LockTaskType;

/**
 * Интерфейс содержающий логику по созданию описания блокировки для НФ
 */
public interface DeclarationDataKeyLockDescriptor {

    /**
     * Создать описание блокировки для НФ в зависимости от типа задачи
     * @param declarationDataId идентификатор НФ
     * @param task              тип задачи
     * @return  строку описания
     */
    String createKeyLockDescription(Long declarationDataId, LockTaskType task);
}
