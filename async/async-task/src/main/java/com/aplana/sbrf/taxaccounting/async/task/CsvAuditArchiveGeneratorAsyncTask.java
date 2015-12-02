package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    protected ReportType getReportType() {
        return ReportType.ARCHIVE_AUDIT;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) {
        return BalancingVariants.LONG;
    }

    @Override
    protected boolean executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer) params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        LogSystemFilter filter = (LogSystemFilter) params.get(AuditService.AsyncNames.LOG_FILTER.name());

        PagingResult<LogSearchResultItem> records = auditService.getLogsByFilter(filter);
        if (records.isEmpty())
            throw new ServiceException("Нет записей за указанную дату.");

        Date firstDate = records.get(records.size() - 1).getLogDate();
        filter.setFromSearchDate(firstDate);

        String uuid = printingService.generateAuditZip(records);
        reportService.createAudit(null, uuid, ReportType.ARCHIVE_AUDIT);

        params.put(AuditService.AsyncNames.LOG_LAST_DATE.name(), records.get(0).getLogDate());
        auditService.removeRecords(
                filter,
                records.get(records.size() - 1),
                records.get(0),
                userInfo);
        /*result.setCountOfRemoveRecords(records.getTotalCount());*/
        return true;
    }

    @Override
    protected String getAsyncTaskName() {
        return "Архивация журнала аудита";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        LogSystemFilter filter = (LogSystemFilter) params.get(AuditService.AsyncNames.LOG_FILTER.name());
        Long count = (Long) params.get(AuditService.AsyncNames.LOG_COUNT.name());

        return String.format(SUCCESS_MSG,
                SDF.format(params.get(AuditService.AsyncNames.LOG_FIRST_DATE.name())),
                SDF.format(
                        params.containsKey(AuditService.AsyncNames.LOG_LAST_DATE.name())
                                ?
                                params.get(AuditService.AsyncNames.LOG_LAST_DATE.name())
                                :
                                SDF.format(filter.getToSearchDate())

                ),
                count);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        LogSystemFilter filter = (LogSystemFilter) params.get(AuditService.AsyncNames.LOG_FILTER.name());

        return String.format(ERROR_MSG,
                SDF.format(params.get(AuditService.AsyncNames.LOG_FIRST_DATE.name())),
                SDF.format(
                        params.containsKey(AuditService.AsyncNames.LOG_LAST_DATE.name())
                                ?
                                params.get(AuditService.AsyncNames.LOG_LAST_DATE.name())
                                :
                                SDF.format(filter.getToSearchDate())

                )
        );
    }
}
