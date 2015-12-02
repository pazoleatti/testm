package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class CsvAuditGeneratorAsyncTask extends AbstractAsyncTask {

    private static final String SUCCESS_MSG =
            "Сформирован ZIP файл с данными журнала аудита по следующим параметрам поиска: %s.";
    private static final String ERROR_MSG =
            "Произошла непредвиденная ошибка при формировании ZIP файла с данными журнала аудита по следующим параметрам поиска: %s. " +
                    "Для запуска процедуры формирования необходимо повторно инициировать формирование данного файла.";
    private static final String EMPTY_DATA_ERROR_MSG = "ZIP файл с данными журнала аудита не сформирован. В журнале аудита отсутствуют данные по заданным параметрам поиска: %s.";

    @Autowired
    TAUserService userService;
    @Autowired
    ReportService reportService;
    @Autowired
    AuditService auditService;
    @Autowired
    PrintingService printingService;

    @Override
    protected ReportType getReportType() {
        return ReportType.CSV_AUDIT;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) {
        return BalancingVariants.LONG;
    }

    @Override
    protected boolean executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        LogSystemFilter filter = (LogSystemFilter)params.get(AuditService.AsyncNames.LOG_FILTER.name());

        PagingResult<LogSearchResultItem> records;
        if (userInfo.getUser().hasRole("ROLE_ADMIN"))
            records = auditService.getLogsByFilter(filter);
        else
            records = auditService.getLogsBusiness(filter, userInfo);
        if (records == null || records.isEmpty()) {
            throw new ServiceException(getMsg(params, true, true, records));
        }

        String uuid = printingService.generateAuditZip(records);
        reportService.createAudit(userInfo.getUser().getId(), uuid, ReportType.CSV_AUDIT);
        records.clear();
        return true;
    }

    @Override
    protected String getAsyncTaskName() {
        return "Формирование отчета по журналу аудита";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return getMsg(params, false, false, null);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        return getMsg(params, true, false, null);
    }

    /**
     * Возвращает текст оповещения, которое будет создано для пользователей, ожидающих выполнение задачи.
     *
     * @param params параметры задачи
     * @param isError признак ошибки
     * @param checkRecords учитывать ли наличие данных для фильтра
     * @param records данные для фильтра
     * @return текст сообещения
     */
    private String getMsg(Map<String, Object> params, boolean isError, boolean checkRecords, PagingResult<LogSearchResultItem> records) {
        String msg;
        if (checkRecords && (records == null || records.isEmpty())) {
            msg = EMPTY_DATA_ERROR_MSG;
        } else {
            msg = (isError ? ERROR_MSG : SUCCESS_MSG);
        }

        return String.format(msg, params.get(AuditService.AsyncNames.SEARCH_CRITERIA.name()));
    }
}
