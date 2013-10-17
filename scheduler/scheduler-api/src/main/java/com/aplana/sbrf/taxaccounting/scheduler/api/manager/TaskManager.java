package com.aplana.sbrf.taxaccounting.scheduler.api.manager;

import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
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
    public Long createTask(TaskContext taskContext) throws TaskSchedulingException;

    /**
     * Удаляет указанную задачу из планировщика
     * @param taskId идентификатор задачи
     * @throws TaskSchedulingException
     */
    public void deleteTask(Long taskId) throws TaskSchedulingException;

    /**
     * Немедленно запускает указанную задачу
     * @param taskId идентификатор задачи
     * @throws TaskSchedulingException
     */
    public void startTask(Long taskId) throws TaskSchedulingException;

    /**
     * Останавливает указанную задачу
     * @param taskId идентификатор задачи
     * @throws TaskSchedulingException
     */
    public void stopTask(Long taskId) throws TaskSchedulingException;

    /**
     * Возобновляет выполнение указанной задачи по расписанию
     * @param taskId идентификатор задачи
     * @throws TaskSchedulingException
     */
    public void resumeTask(Long taskId) throws TaskSchedulingException;

    /**
     * Возвращает данные о запланированной задаче
     * @param taskId идентификатор задачи
     * @return данные о задаче
     * @throws TaskSchedulingException
     */
    public TaskData getTaskData(Long taskId) throws TaskSchedulingException;

    /**
     * Возвращает данные обо всех запланированных задачах
     * @return список данных о задачах
     * @throws TaskSchedulingException
     */
    public List<TaskData> getAllTasksData() throws TaskSchedulingException;

    /**
     * Обновление данных задачи
     * @param taskId идентификатор задачи
     * @param taskContext новые параметры задачи
     * @throws TaskSchedulingException
     */
    public void updateTask(Long taskId, TaskContext taskContext) throws TaskSchedulingException;

    /**
     * Проверяет существование задачи по ее уникальному имени
     * @param taskName уникальное имя задачи
     * @return задача существует?
     * @throws TaskSchedulingException
     */
    public Boolean isTaskExist(String taskName) throws TaskSchedulingException;

    /**
     * Проверяет существование задачи по ее идентификатору
     * @param taskId идентификатор задачи
     * @return задача существует?
     * @throws TaskSchedulingException
     */
    public Boolean isTaskExist(Long taskId) throws TaskSchedulingException;
}
