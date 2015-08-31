package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
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
    private AsyncManager asyncManager;

    public PrintAuditDataHandler() {
        super(PrintAuditDataAction.class);
    }

    @Override
    public PrintAuditDataResult execute(PrintAuditDataAction action, ExecutionContext executionContext) throws ActionException {
        PrintAuditDataResult result = new PrintAuditDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        LogSystemAuditFilter filter = action.getLogSystemFilter();
        StringBuilder builder = new StringBuilder();
        for (Long aLong : filter.getAuditFieldList()) {
            builder.append(AuditFieldList.fromId(aLong).getName()).append(", ");
        }
        String fields = builder.substring(0, builder.toString().length() - 2);

        String searchCriteria = String.format(SEARCH_CRITERIA,
                SDF.format(filter.getFromSearchDate()),
                SDF.format(filter.getToSearchDate()),
                filter.getFilter() != null ? filter.getFilter() : "Не задано",
                filter.getOldLogSystemAuditFilter() == null ? "Нет" : "Да",
                fields
        );
        Logger logger = new Logger();
        long recordsCount = auditService.getCountRecords(action.getLogSystemFilter().convertTo(), userInfo);
        if (recordsCount==0) {
            result.setLogUuid(null);
            return result;
        }
        String key = LockData.LockObjects.LOG_SYSTEM_CSV.name() + "_" + userInfo.getUser().getId();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
        params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
        params.put(AuditService.AsyncNames.LOG_FILTER.name(), action.getLogSystemFilter().convertTo());
        params.put(AuditService.AsyncNames.LOG_COUNT.name(), recordsCount);
        LockData lockData;
        if (reportService.getAudit(userInfo, ReportType.CSV) != null){
            logger.error("Для этого пользователя уже есть отчет по ЖА, проверьте выгрузку.");
            result.setLogUuid(logEntryService.save(logger.getEntries()));
            return result;
        }
        try {
            if ((lockData = lockDataService.lock(key, userInfo.getUser().getId(),
                    String.format(
                            LockData.DescriptionTemplate.LOG_SYSTEM_CSV.getText(),
                            searchCriteria
                    ),
                    LockData.State.IN_QUEUE.getText(),
                    lockDataService.getLockTimeout(LockData.LockObjects.LOG_SYSTEM_CSV))) == null) {
                lockData = lockDataService.getLock(key);
                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                BalancingVariants balancingVariant = asyncManager.checkCreate(ReportType.CSV_AUDIT.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                asyncManager.executeAsync(ReportType.CSV_AUDIT.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params, balancingVariant);
                lockDataService.updateQueue(key, lockData.getDateLock(), balancingVariant);
            } else {
                if (lockData.getUserId() != userInfo.getUser().getId()) {
                    try {
                        lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                    } catch (ServiceException e) {
                        //
                    }
                }
            }
        } catch (AsyncTaskException e) {
            lockDataService.unlock(key, userInfo.getUser().getId());
        }
        logger.info("ZIP файл с данными журнала аудита по параметрам поиска, заданным для отображаемого табличного представления, поставлен в очередь на формирование.");
        result.setLogUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(PrintAuditDataAction printAuditDataAction, PrintAuditDataResult printAuditDataResult, ExecutionContext executionContext) throws ActionException {
        //No implementation
    }
}
