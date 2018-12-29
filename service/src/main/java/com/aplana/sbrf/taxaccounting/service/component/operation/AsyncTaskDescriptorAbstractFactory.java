package com.aplana.sbrf.taxaccounting.service.component.operation;

import com.aplana.sbrf.taxaccounting.model.OperationType;

import java.util.Map;

public interface AsyncTaskDescriptorAbstractFactory {
    /**
     * Создать описание асинхронной задачи для НФ в зависимости от типа задачи
     * @param params        параметры для формирования описания
     * @param operationType     тип задачи
     * @return  строку описания
     */
    String makeDeclarationDescription(Map<String, Object> params, OperationType operationType);

    /**
     * Создать описание асинхронной задачи для транспортного файла
     * @param params параметры для формирования описания
     * @return  строку описания
     */
    String makeTransportFileDescription(Map<String, Object> params);

    /**
     * Создать описание асинхронной задачи для спецотчета по ФЛ в зависимости от типа задачи
     * @param params        параметры для формирования описания
     * @param operationType     тип задачи
     * @return  строку описания
     */
    String makeSpecReportByPersonDescription(Map<String, Object> params, OperationType operationType);

    /**
     * Создать описание асинхронной задачи для спецотчета по ФЛ в зависимости от типа задачи
     * @param params        параметры для формирования описания
     * @param operationType     тип задачи
     * @return  строку описания
     */
    String makeCreateReportsDescription(Map<String, Object> params, OperationType operationType);
}
