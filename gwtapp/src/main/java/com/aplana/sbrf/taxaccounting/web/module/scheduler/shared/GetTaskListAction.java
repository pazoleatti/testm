package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение списка задач планировщика
 * @author dloshakarev
 */
public class GetTaskListAction extends UnsecuredActionImpl<GetTaskListResult> implements ActionName {

    @Override
    public String getName() {
        return "Получение списка задач планировщика";
    }
}
