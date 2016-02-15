package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CheckDeclarationDataHandler extends AbstractActionHandler<CheckDeclarationDataAction, CheckDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private TAUserService userService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm z");

    public CheckDeclarationDataHandler() {
        super(CheckDeclarationDataAction.class);
    }

    @Override
    public CheckDeclarationDataResult execute(final CheckDeclarationDataAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.CHECK_DEC;
		CheckDeclarationDataResult result = new CheckDeclarationDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        LockData lockDataAccept = lockDataService.getLock(declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), DeclarationDataReportType.ACCEPT_DEC));
        if (lockDataAccept == null) {
            String uuidXml = reportService.getDec(userInfo, action.getDeclarationId(), DeclarationDataReportType.XML_DEC);
            if (uuidXml != null) {
                String keyTask = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), ddReportType);
                Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationDataService.getTaskName(ddReportType, action.getTaxType()), userInfo, action.isForce(), logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.LOCKED);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                } else {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("declarationDataId", action.getDeclarationId());
                    asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                        @Override
                        public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                            return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                    declarationDataService.getDeclarationFullName(action.getDeclarationId(), ddReportType),
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
                            return declarationDataService.getTaskName(ddReportType, action.getTaxType());
                        }
                    });
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
            }
        } else {
            try {
                lockDataService.addUserWaitingForLock(lockDataAccept.getKey(), userInfo.getUser().getId());
            } catch (Exception e) {
            }
            logger.error(
                    String.format(
                            LockData.LOCK_CURRENT,
                            sdf.format(lockDataAccept.getDateLock()),
                            userService.getUser(lockDataAccept.getUserId()).getName(),
                            declarationDataService.getTaskName(DeclarationDataReportType.ACCEPT_DEC, action.getTaxType()))
            );
            throw new ServiceLoggerException("Для текущего экземпляра %s запущена операция, при которой ее проверка невозможна", logEntryService.save(logger.getEntries()), action.getTaxType().getDeclarationShortName());
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
	    return result;
    }

    @Override
    public void undo(CheckDeclarationDataAction action, CheckDeclarationDataResult result, ExecutionContext context)
			throws ActionException {
        // Nothing!
    }
}
