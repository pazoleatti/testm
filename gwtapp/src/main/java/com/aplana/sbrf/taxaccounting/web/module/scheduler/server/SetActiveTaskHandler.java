package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.service.SchedulerTaskService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.SetActiveTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.SetActiveTaskResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Обновление признака активности задачи планировщика
 *
 * @author dkoshkarev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class SetActiveTaskHandler extends AbstractActionHandler<SetActiveTaskAction, SetActiveTaskResult> {

    @Autowired
    private SchedulerTaskService schedulerTaskService;
    @Autowired
    private SchedulerService schedulerService;

    public SetActiveTaskHandler() {
        super(SetActiveTaskAction.class);
    }

    @Override
    public SetActiveTaskResult execute(SetActiveTaskAction action, ExecutionContext executionContext) throws ActionException {
        SetActiveTaskResult result = new SetActiveTaskResult();
        schedulerTaskService.updateActiveByIds(action.isActive(), action.getTasksIds());
        schedulerService.updateAllTask();
        return result;
    }

    @Override
    public void undo(SetActiveTaskAction action, SetActiveTaskResult result, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}