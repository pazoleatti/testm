package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Хендлеры формы "Планировщик задач"
 * @author dloshkarev
 */
public interface TaskListUiHandlers extends UiHandlers {

    /**
     * Выю пытается остановить задачу
     */
    void onStopTask();

    /**
     * Выю пытается возобновить задачу
     */
    void onResumeTask();

    /**
     * Обновление списка задач
     */
    void onUpdateTask();
}
