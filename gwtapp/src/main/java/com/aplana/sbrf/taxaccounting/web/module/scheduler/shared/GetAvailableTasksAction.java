package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение списка задач, доступных для планирования
 * @author dloshkarev
 */
public class GetAvailableTasksAction extends UnsecuredActionImpl<GetAvailableTasksResult> implements ActionName {
    @Override
    public String getName() {
        return "Получение списка задач, доступных для планирования";
    }
}
