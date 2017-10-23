package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateFormsDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateFormsDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class CreateFormsDeclarationHandler extends AbstractActionHandler<CreateFormsDeclarationAction, CreateFormsDeclarationResult> {

	public CreateFormsDeclarationHandler() {
		super(CreateFormsDeclarationAction.class);
	}


    @Autowired
    private SecurityService securityService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

	@Override
	public CreateFormsDeclarationResult execute(final CreateFormsDeclarationAction action, ExecutionContext executionContext) throws ActionException {
        final AsyncTaskType reportType = AsyncTaskType.CREATE_FORMS_DEC;
        CreateFormsDeclarationResult result = new CreateFormsDeclarationResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.getLast(action.getDepartmentId(),
                action.getReportPeriodId());
        if (departmentReportPeriod == null) {
            throw new ActionException("Не удалось определить налоговый период.");
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("declarationTypeId", action.getDeclarationTypeId());
        params.put("departmentReportPeriodId", departmentReportPeriod.getId());

        String keyTask = declarationDataService.generateAsyncTaskKey(action.getDeclarationTypeId(), action.getReportPeriodId(), action.getDepartmentId());
        Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, action.isForce(), logger);
        if (restartStatus != null && restartStatus.getFirst()) {
            result.setStatus(false);
            result.setRestartMsg(restartStatus.getSecond());
        } else if (restartStatus != null && !restartStatus.getFirst()) {
            result.setStatus(true);
        } else {
            result.setStatus(true);
            asyncManager.executeTask(keyTask, reportType, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                @Override
                public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                    return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
                }
            });
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
	}

	@Override
	public void undo(CreateFormsDeclarationAction action, CreateFormsDeclarationResult result, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
