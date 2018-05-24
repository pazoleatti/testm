package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Формирование Excel-отчета по справочнику
 */
@Component("ExcelReportRefBookAsyncTask")
public class ExcelReportRefBookAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private PrintingService printingService;

    @Autowired
    private AsyncManager asyncManager;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.EXCEL_REF_BOOK;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        long refBookId = (Long) params.get("refBookId");
        String filter = (String) params.get("filter");
        Date version = (Date) params.get("version");
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBookId);
        if (filter.isEmpty())
            filter = null;
        Long value = (long) refBookDataProvider.getRecordsCount(version, filter) * refBookFactory.get(refBookId).getAttributes().size();
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
        RefBookAttribute sortAttribute = null;
        if (params.containsKey("sortAttribute"))
            sortAttribute = refBookFactory.get(refBookId).getAttribute((Long) params.get("sortAttribute"));
        Boolean isSortAscending = (Boolean) params.get("isSortAscending");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        if (filter.isEmpty())
            filter = null;

        String uuid = printingService.generateRefBookExcel(refBookId, version, filter, searchPattern, sortAttribute, isSortAscending, new LockStateLogger() {
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
        Date version = taskData.getParams().get("version") != null ? (Date) taskData.getParams().get("version") : new Date();
        RefBook refBook = refBookFactory.get(refBookId);

        return String.format("Сформирован \"%s\" отчет справочника \"%s\": Версия: %s, Параметр поиска: %s",
                getAsyncTaskType().getName(), refBook.getName(), version != null ? SDF_DD_MM_YYYY.get().format(version) : "нет",
                searchPattern != null && !searchPattern.isEmpty() ? searchPattern : "не задан");
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        long refBookId = (Long) taskData.getParams().get("refBookId");
        String searchPattern = (String) taskData.getParams().get("searchPattern");
        Date version = taskData.getParams().get("version") != null ? (Date) taskData.getParams().get("version") : new Date();
        RefBook refBook = refBookFactory.get(refBookId);

        return String.format("Произошла непредвиденная ошибка при формировании \"%s\" отчета справочника \"%s\": Версия: %s, Параметр поиска: %s",
                getAsyncTaskType().getName(), refBook.getName(), version != null ? SDF_DD_MM_YYYY.get().format(version) : "нет",
                searchPattern != null && !searchPattern.isEmpty() ? searchPattern : "не задан");
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long refBookId = (Long) params.get("refBookId");
        RefBook refBook = refBookFactory.get(refBookId);
        return String.format(getAsyncTaskType().getDescription(), refBook.getName());
    }
}
