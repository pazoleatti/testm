package com.aplana.sbrf.taxaccounting.scheduler.api.task;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.FormElement;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;

import java.util.List;
import java.util.Map;

/**
 * Внешний интерфейс, определяющий логику выполнения задачи
 * @author dloshkarev
 */
public interface UserTask {
    /**
     * Метод реализующий логику задачи. Вызывается планировщиком.
     * @param params параметрвы выполнения задачи
     * @throws TaskExecutionException
     */
    void execute(Map<String, TaskParam> params, int userId) throws TaskExecutionException;

    /**
     * Возвращает название задачи. Необходимо для отображения списка задач
     * @return название задачи
     */
    String getTaskName();

    /**
     * Возвращает название класса задачи. Необходимо для отображения списка задач
     * @return название класса задачи
     */
    String getTaskClassName();

    /**
     * Возвращает список параметров необходимых для задачи
     * @return
     */
    List<FormElement> getParams(TAUserInfo userInfo);
}
