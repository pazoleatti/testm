package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.model.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateReportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateReportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author lhaziev
 *
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class CreateReportDeclarationHandler extends AbstractActionHandler<CreateReportAction, CreateReportResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    public CreateReportDeclarationHandler() {
        super(CreateReportAction.class);
    }

    @Override
    public CreateReportResult execute(final CreateReportAction action, ExecutionContext executionContext) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(action.getType());
        CreateReportResult result = new CreateReportResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationDataId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationDataId());
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
        if (ddReportType.isSubreport()) {
            DeclarationData declaration = declarationDataService.get(action.getDeclarationDataId(), userInfo);
            ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), action.getType()));
        } else if (ddReportType.equals(DeclarationDataReportType.PDF_DEC) && !declarationDataService.isVisiblePDF(declarationDataService.get(action.getDeclarationDataId(), userInfo), userInfo)) {
            throw new ActionException("Данное действие недоступно");
        }
        Logger logger = new Logger();
        String uuidXml = reportService.getDec(userInfo, action.getDeclarationDataId(), DeclarationDataReportType.XML_DEC);
        if (uuidXml != null) {
            final String uuid = reportService.getDec(userInfo, action.getDeclarationDataId(), ddReportType);
            if (uuid != null && !action.isCreate()) {
                result.setStatus(CreateAsyncTaskStatus.EXIST);
                return result;
            } else {
                String keyTask = declarationDataService.generateAsyncTaskKey(action.getDeclarationDataId(), ddReportType);
                Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationDataService.getAsyncTaskName(ddReportType, action.getTaxType()), userInfo, action.isForce(), logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.LOCKED);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                } else {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("declarationDataId", action.getDeclarationDataId());
                    if (ddReportType.isSubreport()) {
                        params.put("alias", ddReportType.getReportAlias());
                        params.put("viewParamValues", new LinkedHashMap<String, String>());
                        if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
                            params.put("subreportParamValues", action.getSubreportParamValues());
                            if (action.getSelectedRow() != null) {
                                params.put("selectedRecord", action.getSelectedRow());
                            }
                        }
                    }
                    asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, false, userInfo, logger, new AsyncTaskHandler() {
                        @Override
                        public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                            return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                    declarationDataService.getDeclarationFullName(action.getDeclarationDataId(), ddReportType),
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
                            if (uuid != null) {
                                reportService.deleteDec(uuid);
                            }
                        }

                        @Override
                        public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                            return declarationDataService.getAsyncTaskName(ddReportType, action.getTaxType());
                        }
                    });
                }
            }
        } else {
            result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction searchAction, CreateReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }
}
