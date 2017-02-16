package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.AsyncTaskManagerService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.RecalculateDeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.RecalculateDeclarationListResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class RecalculateDeclarationListHandler extends AbstractActionHandler<RecalculateDeclarationListAction, RecalculateDeclarationListResult> {
    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    public RecalculateDeclarationListHandler() {
        super(RecalculateDeclarationListAction.class);
    }

    @Override
    public RecalculateDeclarationListResult execute(final RecalculateDeclarationListAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.XML_DEC;
        final RecalculateDeclarationListResult result = new RecalculateDeclarationListResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        final Logger logger = new Logger();
        final String taskName = declarationDataService.getTaskName(ddReportType, action.getTaxType());
        for (Long id: action.getDeclarationIds()) {
            final Long declarationId = id;
            logger.info("Постановка операции \"%s\" в очередь на исполнение для %s:", taskName, declarationDataService.getDeclarationFullName(declarationId, null));
            try {
                try {
                    declarationDataService.preCalculationCheck(logger, declarationId, userInfo);
                } catch (Exception e) {
                    logger.error("Налоговая форма не может быть рассчитана");
                }
                String keyTask = declarationDataService.generateAsyncTaskKey(declarationId, ddReportType);
                Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationDataService.getTaskName(ddReportType, action.getTaxType()), userInfo, false, logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    logger.warn("Данная операция уже запущена");
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    // задача уже была создана, добавляем пользователя в получатели
                } else {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("declarationDataId", declarationId);
                    params.put("docDate", action.getDocDate());
                    asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                        @Override
                        public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                            return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                    declarationDataService.getDeclarationFullName(declarationId, ddReportType),
                                    LockData.State.IN_QUEUE.getText());
                        }

                        @Override
                        public void executePostCheck() {
                            logger.error("Найдены запущенные задач, которые блокирует выполнение операции.");
                        }

                        @Override
                        public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                            return declarationDataService.checkExistTask(declarationId, reportType, logger);
                        }

                        @Override
                        public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                            declarationDataService.interruptTask(declarationId, userInfo, reportType, TaskInterruptCause.DECLARATION_RECALCULATION);
                        }

                        @Override
                        public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                            return declarationDataService.getTaskName(ddReportType, action.getTaxType());
                        }
                    });
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(RecalculateDeclarationListAction action, RecalculateDeclarationListResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
