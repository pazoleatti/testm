package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.AsyncTaskManagerService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.AcceptDeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.AcceptDeclarationListResult;
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
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class AcceptDeclarationListHandler extends AbstractActionHandler<AcceptDeclarationListAction, AcceptDeclarationListResult> {
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
    private AsyncTaskManagerService asyncTaskManagerService;

    public AcceptDeclarationListHandler() {
        super(AcceptDeclarationListAction.class);
    }

    @Override
    public AcceptDeclarationListResult execute(final AcceptDeclarationListAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.ACCEPT_DEC;
        final AcceptDeclarationListResult result = new AcceptDeclarationListResult();
        final Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        final String taskName = declarationDataService.getTaskName(ddReportType, action.getTaxType());
        for (Long id: action.getDeclarationIds()) {
            if (declarationDataService.existDeclarationData(id)) {
                final Long declarationId = id;
                if (action.isAccepted()) {
                    logger.info("Постановка операции \"%s\" в очередь на исполнение для %s:", taskName, declarationDataService.getDeclarationFullName(declarationId, null));
                    try {
                        String uuidXml = reportService.getDec(userInfo, declarationId, DeclarationDataReportType.XML_DEC);
                        if (uuidXml != null) {
                            DeclarationData declarationData = declarationDataService.get(declarationId, userInfo);
                            if (!declarationData.getState().equals(State.ACCEPTED)) {
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
                                            logger.error("Найдены запущенные задач, которые блокирует выполнение операции.");
                                        }

                                        @Override
                                        public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                                            return declarationDataService.checkExistTask(declarationId, reportType, logger);
                                        }

                                        @Override
                                        public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                                            declarationDataService.interruptTask(declarationId, userInfo, reportType, TaskInterruptCause.DECLARATION_ACCEPT);
                                        }

                                        @Override
                                        public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                                            return declarationDataService.getTaskName(ddReportType, action.getTaxType());
                                        }
                                    });
                                }
                            } else {
                                logger.error("Налоговая форма уже находиться в статусе \"%s\".", State.ACCEPTED.getTitle());
                            }
                        } else {
                            logger.error("Экземпляр налоговой формы не заполнен данными.");
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                } else {
                    logger.info("Выполяется операция \"%s\" для %s:", "Отмена принятия", declarationDataService.getDeclarationFullName(declarationId, null));
                    try {
                        declarationDataService.cancel(logger, declarationId, securityService.currentUserInfo());
                        logger.info("Налоговая форма успешно переведена в статус \"%s\".", State.CREATED.getTitle());
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, id);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(AcceptDeclarationListAction action, AcceptDeclarationListResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
