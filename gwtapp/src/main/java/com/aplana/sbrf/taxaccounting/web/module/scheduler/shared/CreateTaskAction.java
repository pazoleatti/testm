package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

/**
 * Создание новой задачи для планировщика
 * @author dloshkarev
 */
public class CreateTaskAction extends TaskData<CreateTaskResult> {
    @Override
    public String getName() {
        return "Создание задачи планировщика";
    }
}
