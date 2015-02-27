package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

@Component("CsvAuditArchiveGeneratorAsyncTaskSpring")
@Transactional
public class CsvAuditArchiveGeneratorAsyncTaskSpring extends AbstractAsyncTask {
    @Autowired
    PrintingService printingService;
    @Autowired
    TAUserService userService;
    @Autowired
    ReportService reportService;
    @Autowired
    AuditService auditService;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
    private static final String SUCCESS_MSG =
            "Архивация журнала аудита за период %s-%s выполнена успешно (архивировано %d записей). Данные перенесены в файл архива.";
    private static final String ERROR_MSG =
            "Произошла непредвиденная ошибка при архивации журнала аудита за период %s-%s. Для запуска процедуры архивации необходимо повторно инициировать перенос в архив";

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        log.debug("CsvAuditArchiveGeneratorAsyncTaskSpring has been started");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        LogSystemFilter filter = (LogSystemFilter)params.get(AuditService.AsyncNames.LOG_FILTER.name());
        String oldUuid = reportService.getAudit(userInfo, ReportType.ARCHIVE_AUDIT);
        if ( oldUuid!=null){
            reportService.deleteAudit(oldUuid);
        }

        PagingResult<LogSearchResultItem> records = auditService.getLogsByFilter(filter);
        String uuid = printingService.generateAuditCsv(records);
        reportService.createAudit(userInfo.getUser().getId(), uuid, ReportType.ARCHIVE_AUDIT);

        auditService.removeRecords(
                filter,
                records.get(0),
                records.get(records.size()-1),
                userInfo);
        //Чтобы вывести в уведомлении
        records.clear();
        log.debug("CsvAuditArchiveGeneratorAsyncTaskSpring has been finished");
    }

    @Override
    protected String getAsyncTaskName() {
        return "Архивация журнала аудита";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        LogSystemFilter filter = (LogSystemFilter)params.get(AuditService.AsyncNames.LOG_FILTER.name());
        Long count = (Long)params.get(AuditService.AsyncNames.LOG_COUNT.name());

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
