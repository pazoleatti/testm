package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataResult;
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
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
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
    private AsyncManager asyncManager;

    @Autowired
    private AsyncTaskDao asyncTaskDao;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        }
    };

    public CheckDeclarationDataHandler() {
        super(CheckDeclarationDataAction.class);
    }

    @Override
    public CheckDeclarationDataResult execute(final CheckDeclarationDataAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.CHECK_DEC;
		CheckDeclarationDataResult result = new CheckDeclarationDataResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationId());
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        LockData lockDataAccept = lockDataService.getLock(declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), DeclarationDataReportType.ACCEPT_DEC));
        if (lockDataAccept == null) {
            String uuidXml = reportService.getDec(action.getDeclarationId(), DeclarationDataReportType.XML_DEC);
            if (uuidXml != null) {
                String keyTask = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), ddReportType);
                Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, action.isForce(), logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.LOCKED);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                } else {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("declarationDataId", action.getDeclarationId());
                    asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                        @Override
                        public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                            return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                    declarationDataService.getDeclarationFullName(action.getDeclarationId(), ddReportType));
                        }
                    });
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
            }
        } else {
            try {
                asyncManager.addUserWaitingForTask(lockDataAccept.getTaskId(), userInfo.getUser().getId());
            } catch (Exception e) {
                logger.error(e);
            }
            AsyncTaskData acceptTaskData = asyncTaskDao.getLightTaskData(lockDataAccept.getTaskId());
            logger.error(
                    String.format(
                            AsyncTask.LOCK_CURRENT,
                            sdf.get().format(lockDataAccept.getDateLock()),
                            userService.getUser(lockDataAccept.getUserId()).getName(),
                            acceptTaskData.getDescription())
            );
            throw new ServiceLoggerException("Для текущего экземпляра налоговой формы запущена операция, при которой ее проверка невозможна", logEntryService.save(logger.getEntries()));
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
