package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.RecalculateDeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.RecalculateDeclarationListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class RecalculateDeclarationListHandler extends AbstractActionHandler<RecalculateDeclarationListAction, RecalculateDeclarationListResult> {

    private static final Log LOG = LogFactory.getLog(RecalculateDeclarationListHandler.class);

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncManager asyncManager;

    public RecalculateDeclarationListHandler() {
        super(RecalculateDeclarationListAction.class);
    }

    @Override
    public RecalculateDeclarationListResult execute(final RecalculateDeclarationListAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.XML_DEC;
        final RecalculateDeclarationListResult result = new RecalculateDeclarationListResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        final Logger logger = new Logger();
        for (Long id : action.getDeclarationIds()) {
            if (declarationDataService.existDeclarationData(id)) {
                final Long declarationId = id;
                final String prefix = String.format("Постановка операции \"Расчет налоговой формы\" для формы № %d в очередь на исполнение: ", declarationId);
                try {
                    try {
                        declarationDataService.preCalculationCheck(logger, declarationId, userInfo);
                    } catch (Exception e) {
                        logger.error(prefix + "Налоговая форма не может быть рассчитана");
                    }
                    String keyTask = declarationDataService.generateAsyncTaskKey(declarationId, ddReportType);
                    Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, false, logger);
                    if (restartStatus != null && restartStatus.getFirst()) {
                        logger.warn(prefix + "Данная операция уже запущена");
                    } else if (restartStatus != null && !restartStatus.getFirst()) {
                        // задача уже была создана, добавляем пользователя в получатели
                    } else {
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("declarationDataId", declarationId);
                        params.put("docDate", action.getDocDate());
                        asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                            @Override
                            public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                                return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                        declarationDataService.getDeclarationFullName(declarationId, ddReportType));
                            }

                            @Override
                            public void postCheckProcessing() {
                                logger.error(prefix + "Найдены запущенные задачи, которые блокирует выполнение операции.");
                            }

                            @Override
                            public boolean checkExistTasks(AsyncTaskType reportType, TAUserInfo userInfo, Logger logger) {
                                return declarationDataService.checkExistAsyncTask(declarationId, reportType, logger);
                            }

                            @Override
                            public void interruptTasks(AsyncTaskType reportType, TAUserInfo userInfo) {
                                declarationDataService.interruptAsyncTask(declarationId, userInfo, reportType, TaskInterruptCause.DECLARATION_RECALCULATION);
                            }
                        });
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
    public void undo(RecalculateDeclarationListAction action, RecalculateDeclarationListResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
