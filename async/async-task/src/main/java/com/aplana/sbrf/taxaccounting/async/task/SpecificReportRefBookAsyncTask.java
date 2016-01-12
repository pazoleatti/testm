package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

/**
 * @author lhaziev
 */
public abstract class SpecificReportRefBookAsyncTask extends AbstractAsyncTask  {

    @Autowired
    private TAUserService userService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private PrintingService printingService;

    @Autowired
    private LockDataService lockService;

    @Override
    protected ReportType getReportType() {
        return ReportType.SPECIFIC_REPORT_REF_BOOK;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException {
        long refBookId = (Long)params.get("refBookId");
        String filter = (String)params.get("filter");
        Date version = (Date)params.get("version");
        String specificReportType = (String)params.get("specificReportType");
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBookId);
        if (filter.isEmpty())
            filter = null;
        Long value = Long.valueOf(refBookDataProvider.getRecordsCount(version, filter));
        String msg = String.format("количество записей справочника(%s) превышает максимально допустимое(%s)!", value, "%s");
        return checkTask(getReportType(), value, refBookFactory.getTaskName(getReportType(), refBookId, specificReportType), msg);
    }

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long refBookId = (Long)params.get("refBookId");
        String filter = (String)params.get("filter");
        String searchPattern = (String)params.get("searchPattern");
        Date version = (Date)params.get("version");
        String specificReportType = (String)params.get("specificReportType");
        RefBookAttribute sortAttribute = null;
        if (params.containsKey("sortAttribute"))
            sortAttribute = refBookFactory.get(refBookId).getAttribute((Long)params.get("sortAttribute"));
        Boolean isSortAscending = (Boolean)params.get("isSortAscending");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        String uuid = printingService.generateRefBookSpecificReport(refBookId, specificReportType, version, filter, searchPattern, sortAttribute, isSortAscending, userInfo, new LockStateLogger() {
            @Override
            public void updateState(String state) {
                lockService.updateState(lock, lockDate, state);
            }
        });
        return new TaskStatus(true, NotificationType.REF_BOOK_REPORT, uuid);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Формирование специфичного отчета формы";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        long refBookId = (Long)params.get("refBookId");
        String searchPattern = (String)params.get("searchPattern");
        Date version = (Date)params.get("version");
        String specificReportType = (String)params.get("specificReportType");
        RefBook refBook = refBookFactory.get(refBookId);

        return String.format("Сформирован отчет \"%s\" справочника \"%s\": Версия: %s, Фильтр: \"%s\"", specificReportType, refBook.getName(), SDF_DD_MM_YYYY.format(version), searchPattern);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        long refBookId = (Long)params.get("refBookId");
        String searchPattern = (String)params.get("searchPattern");
        Date version = (Date)params.get("version");
        String specificReportType = (String)params.get("specificReportType");
        RefBook refBook = refBookFactory.get(refBookId);

        return String.format("Произошла непредвиденная ошибка при формировании отчета \"%s\" справочника \"%s\": Версия: %s, Фильтр: \"%s\"", specificReportType, refBook.getName(), SDF_DD_MM_YYYY.format(version), searchPattern);
    }
}
