package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.apache.commons.lang3.StringUtils;
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
    private CommonRefBookService commonRefBookService;

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
        String searchPattern = (String) params.get("searchPattern");
        boolean exactSearch = (Boolean) params.get("exactSearch");
        Map<String, String> extraParams = (Map<String, String>) params.get("extraParams");
        Date version = (Date) params.get("version");
        Long value = (long) commonRefBookService.getRecordsCount(refBookId, version, searchPattern, exactSearch, extraParams) * commonRefBookService.get(refBookId).getAttributes().size();
        String msg = String.format("количество выгружаемых ячеек(%s) превышает максимально допустимое(%s)!", value, "%s");
        return checkTask(value, taskDescription, msg);
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        long refBookId = (Long) params.get("refBookId");
        String searchPattern = (String) params.get("searchPattern");
        Date version = (Date) params.get("version");
        boolean exactSearch = (Boolean) params.get("exactSearch");
        RefBookAttribute sortAttribute = (RefBookAttribute) taskData.getParams().get("sortAttribute");
        String direction = (String) params.get("direction");
        Map<String, String> extraParams = (Map<String, String>) params.get("extraParams");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        String uuid = printingService.generateRefBookExcel(refBookId, version, searchPattern, exactSearch, extraParams, sortAttribute, direction, new LockStateLogger() {
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
        boolean exactSearch = (Boolean) taskData.getParams().get("exactSearch");
        Date version = taskData.getParams().get("version") != null ? (Date) taskData.getParams().get("version") : null;
        RefBook refBook = commonRefBookService.get(refBookId);

        return String.format("Сформирован \"%s\" отчет справочника \"%s\".%s%s%s",
                getAsyncTaskType().getName(), refBook.getName(),
                version != null ? " Дата актуальности: " + SDF_DD_MM_YYYY.get().format(version) + "," : "",
                StringUtils.isNotEmpty(searchPattern) ? " Параметр поиска: \"" + searchPattern + "\"" : " Параметр поиска: не задан",
                exactSearch ? " (по точному совпадению)" : "");
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        long refBookId = (Long) taskData.getParams().get("refBookId");
        String searchPattern = (String) taskData.getParams().get("searchPattern");
        boolean exactSearch = (Boolean) taskData.getParams().get("exactSearch");
        Date version = taskData.getParams().get("version") != null ? (Date) taskData.getParams().get("version") : null;
        RefBook refBook = commonRefBookService.get(refBookId);

        return String.format("Произошла непредвиденная ошибка при формировании \"%s\" отчета справочника \"%s\".%s%s%s",
                getAsyncTaskType().getName(), refBook.getName(),
                version != null ? " Дата актуальности: " + SDF_DD_MM_YYYY.get().format(version) + "," : "",
                StringUtils.isNotEmpty(searchPattern) ? " Параметр поиска: \"" + searchPattern + "\"" : " Параметр поиска: не задан",
                exactSearch ? " (по точному совпадению)" : "");
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long refBookId = (Long) params.get("refBookId");
        RefBook refBook = commonRefBookService.get(refBookId);
        return String.format(getAsyncTaskType().getDescription(), refBook.getName());
    }

    @Override
    public LockData establishLock(String lockKey, TAUserInfo user, Map<String, Object> params) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean prohibitiveLockExists(Map<String, Object> params, Logger logger) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
