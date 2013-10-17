package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Хендлеры формы "Планировщик задач"
 * @author dloshkarev
 */
public interface TaskListUiHandlers extends UiHandlers {

    /**
     * Выю пытается открыть форму для создания задачи
     */
    void onShowCreateTaskForm();

    /**
     * Выю пытается остановить задачу
     */
    void onStopTask();

    /**
     * Выю пытается возобновить задачу
     */
    void onResumeTask();

    /**
     * Выю пытается запустить задачу
     */
    void onStartTask();

    /**
     * Выю пытается удалить задачу
     */
    void onDeleteTask();
}
