package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;


/**
 * Формирование специфичных отчетов налоговых форм
 */
@Component("SpecificReportRefBookAsyncTask")
public class SpecificReportRefBookAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;
    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private PrintingService printingService;
    @Autowired
    private AsyncManager asyncManager;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.SPECIFIC_REPORT_REF_BOOK;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        long refBookId = (Long) params.get("refBookId");
        String filter = (String) params.get("filter");
        Date version = (Date) params.get("version");
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBookId);
        if (filter.isEmpty())
            filter = null;
        Long value = Long.valueOf(refBookDataProvider.getRecordsCount(version, filter)) * commonRefBookService.get(refBookId).getAttributes().size();
        String msg = String.format("количество выгружаемых ячеек(%s) превышает максимально допустимое(%s)!", value, "%s");
        return checkTask(value, taskDescription, msg);
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        long refBookId = (Long) params.get("refBookId");
        String filter = (String) params.get("filter");
        String searchPattern = (String) params.get("searchPattern");
        Date version = (Date) params.get("version");
        String specificReportType = (String) params.get("specificReportType");
        RefBookAttribute sortAttribute = null;
        if (params.containsKey("sortAttribute"))
            sortAttribute = commonRefBookService.get(refBookId).getAttribute((Long) params.get("sortAttribute"));
        if (filter.isEmpty())
            filter = null;
        Boolean isSortAscending = (Boolean) params.get("isSortAscending");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        String uuid = printingService.generateRefBookSpecificReport(refBookId, specificReportType, version, filter, searchPattern, sortAttribute, isSortAscending, userInfo, new LockStateLogger() {
            @Override
            public void updateState(AsyncTaskState state) {
                asyncManager.updateState(taskData.getId(), state);
            }
        });
        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        long refBookId = (Long) taskData.getParams().get("refBookId");
        String searchPattern = (String) taskData.getParams().get("searchPattern");
        Date version = (Date) taskData.getParams().get("version");
        String specificReportType = (String) taskData.getParams().get("specificReportType");
        RefBook refBook = commonRefBookService.get(refBookId);

        return String.format("Сформирован отчет \"%s\" справочника \"%s\": Версия: %s, Фильтр: \"%s\"", specificReportType, refBook.getName(), SDF_DD_MM_YYYY.get().format(version), searchPattern);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        long refBookId = (Long) taskData.getParams().get("refBookId");
        String searchPattern = (String) taskData.getParams().get("searchPattern");
        Date version = (Date) taskData.getParams().get("version");
        String specificReportType = (String) taskData.getParams().get("specificReportType");
        RefBook refBook = commonRefBookService.get(refBookId);

        return String.format("Произошла %sошибка при формировании отчета \"%s\" справочника \"%s\": Версия: %s, Фильтр: \"%s\"", unexpected ? "непредвиденная " : "", specificReportType, refBook.getName(), SDF_DD_MM_YYYY.get().format(version), searchPattern);
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long refBookId = (Long) params.get("refBookId");
        String specificReportType = (String) params.get("specificReportType");
        String filter = (String) params.get("filter");
        Date version = (Date) params.get("version");
        RefBook refBook = commonRefBookService.get(refBookId);
        return String.format(getAsyncTaskType().getDescription(), specificReportType, refBook.getName(), version, filter);
    }

    @Override
    public LockData lockObject(String lockKey, TAUserInfo user, Map<String, Object> params) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean checkLocks(Map<String, Object> params, Logger logger) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
