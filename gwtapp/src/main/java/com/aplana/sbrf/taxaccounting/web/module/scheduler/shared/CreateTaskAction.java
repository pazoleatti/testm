package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

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
