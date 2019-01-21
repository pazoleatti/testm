package com.aplana.sbrf.taxaccounting.service.component.operation;

import java.util.List;

/**
 * Интерфейс содержающий логику по созданию описания асинхронной задачи для списка отчетных налоговых формы
 */
public interface DeclarationDataReportingMultiModeAsyncTaskDescriptor {
    /**
     * Создать описание асинхронной задачи
     * @param declarationDataIds    список идентификаторов налологовых форм
     * @param name                  наименование задачи
     * @return  строка описания
     */
    String createDescription(List<Long> declarationDataIds, String name);
}
