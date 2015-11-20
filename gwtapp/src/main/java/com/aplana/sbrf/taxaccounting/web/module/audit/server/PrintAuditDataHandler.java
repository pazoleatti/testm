package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.LogSystemAuditFilter;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.PrintAuditDataAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.PrintAuditDataResult;
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

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS', 'ROLE_OPER', 'ROLE_CONTROL')")
public class PrintAuditDataHandler extends AbstractActionHandler<PrintAuditDataAction, PrintAuditDataResult> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");
    private static final String SEARCH_CRITERIA = "От даты: \"%s\", До даты: \"%s\", Критерий поиска: \"%s\", Искать в найденном: \"%s\", Искать по полям: \"%s\"";

    @Autowired
    BlobDataService blobDataService;
    @Autowired
    AuditService auditService;
    @Autowired
    SecurityService securityService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    LogEntryService logEntryService;
    @Autowired
    ReportService reportService;
    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    public PrintAuditDataHandler() {
        super(PrintAuditDataAction.class);
    }

    @Override
    public PrintAuditDataResult execute(PrintAuditDataAction action, ExecutionContext executionContext) throws ActionException {
        final ReportType reportType = ReportType.CSV_AUDIT;
        PrintAuditDataResult result = new PrintAuditDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        LogSystemAuditFilter filter = action.getLogSystemFilter();
        StringBuilder builder = new StringBuilder();
        for (Long aLong : filter.getAuditFieldList()) {
            builder.append(AuditFieldList.fromId(aLong).getName()).append(", ");
        }
        String fields = "";
        if (builder.length()>1){
            fields = builder.substring(0, builder.toString().length() - 2);
        }
        final String searchCriteria = String.format(SEARCH_CRITERIA,
                SDF.format(filter.getFromSearchDate()),
                SDF.format(filter.getToSearchDate()),
                filter.getFilter() != null ? filter.getFilter() : "Не задано",
                filter.getOldLogSystemAuditFilter() == null ? "Нет" : "Да",
                fields.isEmpty()?"Все поля":fields
        );
        Logger logger = new Logger();
        String reportUuid = reportService.getAudit(userInfo, reportType);
        if (reportUuid != null){
            result.setUuid(reportUuid);
            return result;
        }
        long recordsCount = auditService.getCountRecords(action.getLogSystemFilter().convertTo(), userInfo);
        if (recordsCount==0) {
            result.setLogUuid(null);
            return result;
        }

        String keyTask = LockData.LockObjects.LOG_SYSTEM_CSV.name() + "_" + userInfo.getUser().getId();
        Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, reportType.getDescription(), userInfo, action.isForce(), logger);
        if (restartStatus != null && restartStatus.getFirst()) {
            result.setLock(true);
            result.setRestartMsg(restartStatus.getSecond());
        } else if (restartStatus != null && !restartStatus.getFirst()) {
            result.setLock(false);
        } else {
            result.setLock(false);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(AuditService.AsyncNames.LOG_FILTER.name(), action.getLogSystemFilter().convertTo());
            params.put(AuditService.AsyncNames.LOG_COUNT.name(), recordsCount);
            params.put(AuditService.AsyncNames.SEARCH_CRITERIA.name(), searchCriteria);
            asyncTaskManagerService.createTask(keyTask, reportType, params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                @Override
                public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                    return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                            String.format(
                                    LockData.DescriptionTemplate.LOG_SYSTEM_CSV.getText(),
                                    searchCriteria
                            ),
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
                    return reportType.getDescription();
                }
            });
        }
        result.setLogUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(PrintAuditDataAction printAuditDataAction, PrintAuditDataResult printAuditDataResult, ExecutionContext executionContext) throws ActionException {
        //No implementation
    }
}
