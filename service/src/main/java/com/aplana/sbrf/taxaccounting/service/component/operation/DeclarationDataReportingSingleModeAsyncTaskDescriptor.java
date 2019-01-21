package com.aplana.sbrf.taxaccounting.service.component.operation;

/**
 * Интерфейс содержающий логику по созданию описания асинхронной задачи для отчетной налоговых формы
 */
public interface DeclarationDataReportingSingleModeAsyncTaskDescriptor {
    /**
     * Создать описание асинхронной задачи
     * @param declarationDataId    идентификтаор НФ
     * @param name                  наименование задачи
     * @return  строка описания
     */
    String createDescription(Long declarationDataId, String name);
}
