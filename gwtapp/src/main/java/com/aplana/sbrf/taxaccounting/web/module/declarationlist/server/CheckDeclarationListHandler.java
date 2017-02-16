package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckDeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckDeclarationListResult;
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
public class CheckDeclarationListHandler extends AbstractActionHandler<CheckDeclarationListAction, CheckDeclarationListResult> {
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
        final String taskName = declarationDataService.getTaskName(ddReportType, action.getTaxType());
        Logger logger = new Logger();
        for (Long id: action.getDeclarationIds()) {
            final Long declarationId = id;
            logger.info("Постановка операции \"%s\" в очередь на исполнение для %s:", taskName, declarationDataService.getDeclarationFullName(declarationId, null));
            try {
                LockData lockDataAccept = lockDataService.getLock(declarationDataService.generateAsyncTaskKey(declarationId, DeclarationDataReportType.ACCEPT_DEC));
                if (lockDataAccept == null) {
                    String uuidXml = reportService.getDec(userInfo, declarationId, DeclarationDataReportType.XML_DEC);
                    if (uuidXml != null) {
                        String keyTask = declarationDataService.generateAsyncTaskKey(declarationId, ddReportType);
                        Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationDataService.getTaskName(ddReportType, action.getTaxType()), userInfo, false, logger);
                        if (restartStatus != null && restartStatus.getFirst()) {
                            logger.warn("Данная операция уже запущена");
                        } else if (restartStatus != null && !restartStatus.getFirst()) {
                            // задача уже была создана, добавляем пользователя в получатели
                        } else {
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("declarationDataId", declarationId);
                            asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                                @Override
                                public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                                    return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                            declarationDataService.getDeclarationFullName(declarationId, ddReportType),
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
                        logger.error("Экземпляр налоговой формы не заполнен данными.");
                    }
                } else {
                    try {
                        lockDataService.addUserWaitingForLock(lockDataAccept.getKey(), userInfo.getUser().getId());
                    } catch (Exception e) {
                    }
                    logger.error(
                            String.format(
                                    LockData.LOCK_CURRENT,
                                    sdf.get().format(lockDataAccept.getDateLock()),
                                    userService.getUser(lockDataAccept.getUserId()).getName(),
                                    declarationDataService.getTaskName(DeclarationDataReportType.ACCEPT_DEC, action.getTaxType()))
                    );
                    logger.error("Запущены операция, при которой выполнение данной операции невозможно");
                }
            } catch (Exception e) {
                logger.error(e);
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
