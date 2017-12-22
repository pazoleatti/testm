package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckDeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckDeclarationListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class CheckDeclarationListHandler extends AbstractActionHandler<CheckDeclarationListAction, CheckDeclarationListResult> {

    private static final Log LOG = LogFactory.getLog(CheckDeclarationListHandler.class);

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

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        }
    };

    public CheckDeclarationListHandler() {
        super(CheckDeclarationListAction.class);
    }

    @Override
    public CheckDeclarationListResult execute(final CheckDeclarationListAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.CHECK_DEC;
        CheckDeclarationListResult result = new CheckDeclarationListResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        for (Long id: action.getDeclarationIds()) {
            if (declarationDataService.existDeclarationData(id)) {
                final Long declarationId = id;
                final String prefix = String.format("Постановка операции \"Проверка налоговой формы\" для формы № %d в очередь на исполнение: ", declarationId);
                try {
                    LockData lockDataAccept = lockDataService.getLock(declarationDataService.generateAsyncTaskKey(declarationId, DeclarationDataReportType.ACCEPT_DEC));
                    if (lockDataAccept == null) {
                        String uuidXml = reportService.getDec(declarationId, DeclarationDataReportType.XML_DEC);
                        if (uuidXml != null) {
                            String keyTask = declarationDataService.generateAsyncTaskKey(declarationId, ddReportType);
                            Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, false, logger);
                            if (restartStatus != null && restartStatus.getFirst()) {
                                logger.warn(prefix + "Данная операция уже запущена");
                            } else if (restartStatus != null && !restartStatus.getFirst()) {
                                // задача уже была создана, добавляем пользователя в получатели
                            } else {
                                Map<String, Object> params = new HashMap<String, Object>();
                                params.put("declarationDataId", declarationId);
                                asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                                    @Override
                                    public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                                        return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                                declarationDataService.getDeclarationFullName(declarationId, ddReportType));
                                    }

                                    @Override
                                    public void postCheckProcessing() {
                                    }

                                    @Override
                                    public boolean checkExistTasks(AsyncTaskType reportType, TAUserInfo userInfo, Logger logger) {
                                        return false;
                                    }

                                    @Override
                                    public void interruptTasks(AsyncTaskType reportType, TAUserInfo userInfo) {
                                    }
                                });
                            }
                        } else {
                            logger.error(prefix + "Экземпляр налоговой формы не заполнен данными.");
                        }
                    } else {
                        try {
                            asyncManager.addUserWaitingForTask(lockDataAccept.getTaskId(), userInfo.getUser().getId());
                        } catch (Exception e) {
                        }
                        AsyncTaskData taskData = asyncManager.getLightTaskData(lockDataAccept.getTaskId());
                        logger.error(
                                String.format(
                                        AsyncTask.LOCK_CURRENT,
                                        sdf.get().format(lockDataAccept.getDateLock()),
                                        userService.getUser(lockDataAccept.getUserId()).getName(),
                                        taskData.getDescription())
                        );
                        logger.error(prefix + "Запущена операция, при которой выполнение данной операции невозможно");
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    logger.error(prefix + e.getMessage());
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, id);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
	    return result;
    }

    @Override
    public void undo(CheckDeclarationListAction action, CheckDeclarationListResult result, ExecutionContext context)
			throws ActionException {
        // Nothing!
    }
}
