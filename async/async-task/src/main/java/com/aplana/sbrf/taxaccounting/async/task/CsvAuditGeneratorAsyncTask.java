package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class CsvAuditGeneratorAsyncTask extends AbstractAsyncTask {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");
    private static final String SUCCESS_MSG =
            "Сформирован ZIP файл с данными журнала аудита по следующим параметрам поиска: " +
                    "От даты %s, До даты %s, Критерий поиска: %s, Искать в найденном: %s, Искать по полям: %s.";
    private static final String ERROR_MSG =
            "Произошла непредвиденная ошибка при формировании ZIP файла с данными журнала аудита по следующим параметрам поиска: " +
                    "От даты %s, До даты %s, Критерий поиска: %s, Искать в найденном: %s, Искать по полям: %s." +
                    "Для запуска процедуры формирования необходимо повторно инициировать формирование данного файла.";

    @Autowired
    TAUserService userService;
    @Autowired
    ReportService reportService;
    @Autowired
    AuditService auditService;
    @Autowired
    PrintingService printingService;

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) {
        return BalancingVariants.SHORT;
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        LogSystemFilter filter = (LogSystemFilter)params.get(AuditService.AsyncNames.LOG_FILTER.name());

        PagingResult<LogSearchResultItem> records;
        if (userInfo.getUser().hasRole("ROLE_ADMIN"))
            records = auditService.getLogsByFilter(filter);
        else
            records = auditService.getLogsBusiness(filter, userInfo);

        String uuid = printingService.generateAuditCsv(records);
        reportService.createAudit(userInfo.getUser().getId(), uuid, ReportType.CSV_AUDIT);
        records.clear();
        log.debug("CsvAuditGeneratorAsyncTaskSpring has been finished");
    }

    @Override
    protected String getAsyncTaskName() {
        return "Отчет по журналу аудита";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        LogSystemFilter filter = (LogSystemFilter)params.get(AuditService.AsyncNames.LOG_FILTER.name());
        StringBuilder builder = new StringBuilder();
        for (Long aLong : filter.getAuditFieldList()){
            builder.append(AuditFieldList.fromId(aLong).getName());
        }
        String fields = builder.substring(0, builder.toString().length()-2);

        return String.format(SUCCESS_MSG,
                SDF.format(filter.getFromSearchDate()),
                SDF.format(filter.getToSearchDate()),
                filter.getFilter(),
                filter.getOldLogSystemFilter()==null?"Нет":"Да",
                fields
        );
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        LogSystemFilter filter = (LogSystemFilter)params.get(AuditService.AsyncNames.LOG_FILTER.name());
        StringBuilder builder = new StringBuilder();
        for (Long aLong : filter.getAuditFieldList()){
            builder.append(AuditFieldList.fromId(aLong).getName());
        }
        String fields = builder.substring(0, builder.toString().length()-2);

        return String.format(ERROR_MSG,
                SDF.format(filter.getFromSearchDate()),
                SDF.format(filter.getToSearchDate()),
                filter.getFilter(),
                filter.getOldLogSystemFilter()==null?"Нет":"Да",
                fields
        );
    }
}
