package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Выгрузка реестра ФЛ в excel-файл
 */
@Component("ExcelReportPersonsAsyncTask")
public class ExcelReportPersonsAsyncTask extends AbstractAsyncTask {
    @Autowired
    private PersonService personService;
    @Autowired
    private PrintingService printingService;

    @Autowired

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.EXCEL_PERSONS;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        RefBookPersonFilter personsFilter = (RefBookPersonFilter) params.get("personsFilter");
        Long value = (long) personService.getPersonsCount(personsFilter);
        if (value == 0) {
            throw new ServiceException("Выполнение операции \"%s\" невозможно, т.к. по заданным параметрам не найдено ни одной записи", taskDescription);
        }
        String msg = String.format("количество отобранных для выгрузки в файл записей (%s) больше, чем разрешенное значение (%s).", value, "%s");
        return checkTask(value, taskDescription, msg);
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        RefBookPersonFilter personsFilter = (RefBookPersonFilter) params.get("personsFilter");
        PagingParams pagingParams = (PagingParams) params.get("pagingParams");
        TAUser user = userService.getUser(taskData.getUserId());
        String uuid = printingService.generateExcelPersons(personsFilter, pagingParams, user);
        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return "Завершена операция \"Выгрузка файла данных Реестра физических лиц в XLSX-формате\"";
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        Throwable throwable = (Throwable) taskData.getParams().get("exceptionThrown");
        return String.format("Не выполнена операция \"Выгрузка файла данных Реестра физических лиц в XLSX-формате\".%s",
                throwable != null ? " Причина: " + throwable.getMessage() : "");
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return "Выгрузка файла данных Реестра физических лиц в XLSX-формате";
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
