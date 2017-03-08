package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskJndiInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetAvailableTasksAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetAvailableTasksResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Получение списка задач, доступных для планирования
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN')")
public class GetAvailableTasksHandler extends AbstractActionHandler<GetAvailableTasksAction, GetAvailableTasksResult> {

    @Autowired
    TaskManager taskManager;

    @Autowired
    SecurityService securityService;

    public GetAvailableTasksHandler() {
        super(GetAvailableTasksAction.class);
    }

    @Override
    public GetAvailableTasksResult execute(GetAvailableTasksAction getAvailableTasksAction, ExecutionContext executionContext) throws ActionException {
        GetAvailableTasksResult result = new GetAvailableTasksResult();
        try {
            TAUserInfo userInfo = securityService.currentUserInfo();
            List<TaskJndiInfo> taskInfo = taskManager.getTasksJndi(userInfo);
            result.setJndiList(taskInfo);
            return result;
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка получения списка задач, доступых для планирования", e);
        }
    }

    @Override
    public void undo(GetAvailableTasksAction getAvailableTasksAction, GetAvailableTasksResult getAvailableTasksResult, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
