package com.aplana.sbrf.taxaccounting.scheduler.api.manager;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskJndiInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;

import java.util.List;

/**
 * Публичный интерфейс для взаимодействия с планировщиком задач
 * @author dloshkarev
 */
public interface TaskManager {

    /**
     * Создает новую задачу в планировщике
     * @param taskContext параметры задачи
     * @return идентификатор задачи
     * @throws TaskSchedulingException
     */
    Long createTask(TaskContext taskContext) throws TaskSchedulingException;

    /**
     * Удаляет указанную задачу из планировщика
     * @param taskId идентификатор задачи
     * @throws TaskSchedulingException
     */
    void deleteTask(Long taskId) throws TaskSchedulingException;

    /**
     * Немедленно запускает указанную задачу
     * @param taskId идентификатор задачи
     * @throws TaskSchedulingException
     */
    void startTask(Long taskId) throws TaskSchedulingException;

    /**
     * Останавливает указанную задачу
     * @param taskId идентификатор задачи
     * @throws TaskSchedulingException
     */
    void stopTask(Long taskId) throws TaskSchedulingException;

    /**
     * Возобновляет выполнение указанной задачи по расписанию
     * @param taskId идентификатор задачи
     * @throws TaskSchedulingException
     */
    void resumeTask(Long taskId) throws TaskSchedulingException;

    /**
     * Возвращает данные о запланированной задаче
     * @param taskId идентификатор задачи
     * @return данные о задаче
     * @throws TaskSchedulingException
     */
    TaskData getTaskData(Long taskId) throws TaskSchedulingException;

    /**
     * Возвращает данные обо всех запланированных задачах
     * @return список данных о задачах
     * @throws TaskSchedulingException
     */
    List<TaskData> getAllTasksData() throws TaskSchedulingException;

    /**
     * Обновление данных задачи
     * @param taskId идентификатор задачи
     * @param taskContext новые параметры задачи
     * @throws TaskSchedulingException
     */
    void updateTask(Long taskId, TaskContext taskContext) throws TaskSchedulingException;

    /**
     * Проверяет существование задачи по ее уникальному имени
     * @param taskName уникальное имя задачи
     * @return задача существует?
     * @throws TaskSchedulingException
     */
    Boolean isTaskExist(String taskName) throws TaskSchedulingException;

    /**
     * Проверяет существование задачи по ее идентификатору
     * @param taskId идентификатор задачи
     * @return задача существует?
     * @throws TaskSchedulingException
     */
    Boolean isTaskExist(Long taskId) throws TaskSchedulingException;

    /**
     * Возвращает информацию о доступных пользовательских задачах
     * @return информация о задачах
     * @throws TaskSchedulingException
     */
    List<TaskJndiInfo> getTasksJndi(TAUserInfo userInfo) throws TaskSchedulingException;

    /**
     * Валидация расписания
     */
    boolean validateSchedule(String schedule) throws TaskSchedulingException;
}
