package com.aplana.sbrf.taxaccounting.service.component.operation;

public interface CreateReportsAsyncTaskDescriptor {
    /**
     * Создать описание асинхронной задачи в зависимости от типа задачи
     * @param departmentReportPeriodId идентификатор периода
     * @param declarationTypeId        идентификатор вида НФ
     * @return  строка описания
     */
    String createDescription(Integer departmentReportPeriodId, Integer declarationTypeId);
}
