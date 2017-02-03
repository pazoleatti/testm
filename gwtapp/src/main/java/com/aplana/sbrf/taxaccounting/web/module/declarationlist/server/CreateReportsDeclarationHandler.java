package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateReportsDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateReportsDeclarationResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateReportsDeclarationHandler extends AbstractActionHandler<CreateReportsDeclarationAction, CreateReportsDeclarationResult> {

	public CreateReportsDeclarationHandler() {
		super(CreateReportsDeclarationAction.class);
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
    private AsyncTaskManagerService asyncTaskManagerService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

	@Override
	public CreateReportsDeclarationResult execute(final CreateReportsDeclarationAction action, ExecutionContext executionContext) throws ActionException {
        final ReportType reportType = ReportType.CREATE_REPORTS_DEC;
        CreateReportsDeclarationResult result = new CreateReportsDeclarationResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(action.getDepartmentId()));
        departmentReportPeriodFilter.setReportPeriodIdList(Arrays.asList(action.getReportPeriodId()));
        departmentReportPeriodFilter.setTaxTypeList(Arrays.asList(action.getTaxType()));
        if (action.getCorrectionDate() != null) {
            departmentReportPeriodFilter.setCorrectionDate(action.getCorrectionDate());
        } else {
            departmentReportPeriodFilter.setIsCorrection(false);
        }
        List<DepartmentReportPeriod> departmentReportPeriods = departmentReportPeriodService.getListByFilter(departmentReportPeriodFilter);

        if (departmentReportPeriods == null || departmentReportPeriods.isEmpty() || departmentReportPeriods.size() > 1) {
            throw new ActionException("Не удалось определить налоговый период.");
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriods.get(0);

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("declarationTypeId", action.getDeclarationTypeId());
        params.put("departmentReportPeriodId", departmentReportPeriod.getId());

        String keyTask = declarationDataService.generateAsyncTaskKey(action.getDeclarationTypeId(), action.getReportPeriodId(), action.getDepartmentId());
        Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationDataService.getTaskName(reportType, action.getTaxType(), params), userInfo, action.isForce(), logger);
        if (restartStatus != null && restartStatus.getFirst()) {
            result.setStatus(false);
            result.setRestartMsg(restartStatus.getSecond());
        } else if (restartStatus != null && !restartStatus.getFirst()) {
            result.setStatus(true);
        } else {
            result.setStatus(true);
            asyncTaskManagerService.createTask(keyTask, reportType, params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                @Override
                public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                    return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                            declarationDataService.getTaskName(reportType, action.getTaxType(), params),
                            LockData.State.IN_QUEUE.getText());
                }

                @Override
                public void executePostCheck() {
                }

                @Override
                public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                    return false;
                }

                @Override
                public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                }

                @Override
                public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                    return declarationDataService.getTaskName(reportType, action.getTaxType(), params);
                }
            });
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
	}

	@Override
	public void undo(CreateReportsDeclarationAction action, CreateReportsDeclarationResult result, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
