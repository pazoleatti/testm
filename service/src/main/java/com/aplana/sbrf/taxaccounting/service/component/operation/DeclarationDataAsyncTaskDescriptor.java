package com.aplana.sbrf.taxaccounting.service.component.operation;

/**
 * Интерфейс содержающий логику по созданию описания асинхронной задачи для НФ
 */
public interface DeclarationDataAsyncTaskDescriptor {

    /**
     * Создать описание асинхронной задачи
     * @param declarationDataId идентификатор НФ
     * @param name              наименование задачи
     * @return  строка описания
     */
    String createDescription(Long declarationDataId, String name);
}
