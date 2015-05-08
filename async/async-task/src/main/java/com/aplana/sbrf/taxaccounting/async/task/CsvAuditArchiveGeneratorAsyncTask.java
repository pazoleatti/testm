package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class CsvAuditArchiveGeneratorAsyncTask extends AbstractAsyncTask {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
    private static final String SUCCESS_MSG =
            "Архивация журнала аудита за период %s-%s выполнена успешно (архивировано %d записей). Данные перенесены в файл архива.";
    private static final String ERROR_MSG =
            "Произошла непредвиденная ошибка при архивации журнала аудита за период %s-%s. Для запуска процедуры архивации необходимо повторно инициировать перенос в архив";

    @Autowired
    private PrintingService printingService;
    @Autowired
    private TAUserService userService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private ReportService reportService;

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) {
        return BalancingVariants.SHORT;
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        log.debug("CsvAuditGeneratorAsyncTaskImpl has been started");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        LogSystemFilter filter = (LogSystemFilter)params.get(AuditService.AsyncNames.LOG_FILTER.name());

        PagingResult<LogSearchResultItem> records = auditService.getLogsByFilter(filter);
        if (records.isEmpty())
            throw new ServiceException("Нет записей за указанную дату.");
        String uuid = printingService.generateAuditCsv(records);
        reportService.createAudit(userInfo.getUser().getId(), uuid, ReportType.CSV_AUDIT);

        auditService.removeRecords(
                filter,
                records.get(0),
                records.get(records.size()-1),
                userInfo);
        /*result.setCountOfRemoveRecords(records.getTotalCount());*/
        log.debug("CsvAuditGeneratorAsyncTaskImpl has been finished");
    }

    @Override
    protected String getAsyncTaskName() {
        return "Архивация журнала аудита";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        LogSystemFilter filter = (LogSystemFilter)params.get(AuditService.AsyncNames.LOG_FILTER.name());
        Integer count = (Integer)params.get(AuditService.AsyncNames.LOG_COUNT.name());

        return String.format(SUCCESS_MSG,
                SDF.format(filter.getFromSearchDate()),
                SDF.format(filter.getToSearchDate()),
                count);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        LogSystemFilter filter = (LogSystemFilter)params.get(AuditService.AsyncNames.LOG_FILTER.name());

        return String.format(ERROR_MSG,
                SDF.format(filter.getFromSearchDate()) ,
                SDF.format(filter.getToSearchDate()));
    }
}
