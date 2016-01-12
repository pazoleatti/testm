package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

/**
 * @author lhaziev
 */
public abstract class CsvReportRefBookAsyncTask1 extends AbstractAsyncTask  {

    private static final Log LOG = LogFactory.getLog(LoadAllTransportDataAsyncTask.class);

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
        return ReportType.CSV_REF_BOOK;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException {
        long refBookId = (Long)params.get("refBookId");
        String filter = (String)params.get("filter");
        Date version = (Date)params.get("version");
        LOG.info(String.format("Получение провайдера для справочника refBookId = %s", refBookId));
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBookId);
        if (filter.isEmpty())
            filter = null;
        LOG.info(String.format("Получение количества записей для справочника refBookId = %s", refBookId));
        Long value = Long.valueOf(refBookDataProvider.getRecordsCount(version, filter));
        String msg = String.format("количество записей справочника(%s) превышает максимально допустимое(%s)!", value, "%s");
        LOG.info(String.format("Определение очереди refBookId = %s", refBookId));
        return checkTask(getReportType(), value, refBookFactory.getTaskName(getReportType(), refBookId, null), msg);
    }

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long refBookId = (Long)params.get("refBookId");
        String filter = (String)params.get("filter");
        Date version = (Date)params.get("version");
        RefBookAttribute sortAttribute = null;
        if (params.containsKey("sortAttribute"))
            sortAttribute = refBookFactory.get(refBookId).getAttribute((Long)params.get("sortAttribute"));
        Boolean isSortAscending = (Boolean)params.get("isSortAscending");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());
        if (filter.isEmpty())
            filter = null;

        String uuid = printingService.generateRefBookCSV(refBookId, version, filter, sortAttribute, isSortAscending, new LockStateLogger() {
            @Override
            public void updateState(String state) {
                lockService.updateState(lock, lockDate, state);
            }
        });
        return new TaskStatus(true, NotificationType.REF_BOOK_REPORT, uuid);
    }

    @Override
    protected String getAsyncTaskName() {
        return String.format("Формирование \"%s\" отчета справочника", getReportType().getName());
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        long refBookId = (Long)params.get("refBookId");
        String searchPattern = (String)params.get("searchPattern");
        Date version = (Date)params.get("version");
        RefBook refBook = refBookFactory.get(refBookId);

        return String.format("Сформирован \"%s\" отчет справочника \"%s\": Версия: %s, Фильтр: \"%s\"", getReportType().getName(), refBook.getName(), SDF_DD_MM_YYYY.format(version), searchPattern);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        long refBookId = (Long)params.get("refBookId");
        String searchPattern = (String)params.get("searchPattern");
        Date version = (Date)params.get("version");
        RefBook refBook = refBookFactory.get(refBookId);

        return String.format("Произошла непредвиденная ошибка при формировании \"%s\" отчета справочника \"%s\": Версия: %s, Фильтр: \"%s\"", getReportType().getName(), refBook.getName(), SDF_DD_MM_YYYY.format(version), searchPattern);
    }
}
