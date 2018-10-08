package com.aplana.sbrf.taxaccounting.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель для работы с типами асинхронных задач
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskTypeData {

    /**
     * Идентификатор типа задачи
     */
    private long id;
    /**
     * Имя типа задачи
     */
    private String name;
    /**
     * Класс-обработчик задачи
     */
    private String handlerBean;
    /**
     * Ограничение на выполнение задачи в очереди быстрых задач
     */
    private Long shortQueueLimit;
    /**
     * Ограничение на выполнение задачи
     */
    private Long taskLimit;

    /**
     * Наименование вида ограничения
     */
    private String limitKind;
}
