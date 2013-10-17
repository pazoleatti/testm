package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Хэндлеры форма "Задача планировщика"
 * @author dloshkarev
 */
public interface TaskUiHandlers extends UiHandlers {

    /**
     * Вью пытается создать новую задачу планировщика
     */
    void onCreateTask();

    /**
     * Вью пытается отменить создание задачи
     */
    void onCancel();

    /**
     * Вью пытается создать добавить новый параметр в задачу
     */
    void onAddParam();

    /**
     * Вью пытается создать добавить новый параметр с заданными данными
     */
    void onAddParam(TaskParamModel param);
}
