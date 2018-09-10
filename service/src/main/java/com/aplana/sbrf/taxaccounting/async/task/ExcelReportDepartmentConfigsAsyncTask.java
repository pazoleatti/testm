package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("ExcelReportDepartmentConfigsAsyncTask")
public class ExcelReportDepartmentConfigsAsyncTask extends AbstractAsyncTask {
    @Autowired
    private PrintingService printingService;
    @Autowired
    private DepartmentConfigService departmentConfigService;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.EXCEL_DEPARTMENT_CONFIGS;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        DepartmentConfigsFilter filter = (DepartmentConfigsFilter) params.get("departmentConfigsFilter");
        Long value = (long) departmentConfigService.fetchCount(filter);
        if (value == 0) {
            throw new ServiceException("Выполнение операции \"%s\" невозможно, т.к. по заданным параметрам не найдено ни одной записи", taskDescription);
        }
        String msg = String.format("количество отобранных для выгрузки в файл записей (%s) больше, чем разрешенное значение (%s). Для успешного выполнения операции установите дополнительный критерий фильтрации.", value, "%s");
        return checkTask(value, taskDescription, msg);
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        DepartmentConfigsFilter filter = (DepartmentConfigsFilter) params.get("departmentConfigsFilter");
        PagingParams pagingParams = (PagingParams) params.get("pagingParams");
        String uuid = printingService.generateExcelDepartmentConfigs(filter, pagingParams);
        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return "Завершена операция \"Выгрузка настроек подразделений в файл формата XLSX\"";
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        Throwable throwable = (Throwable) taskData.getParams().get("exceptionThrown");
        return String.format("Не выполнена операция \"Выгрузка настроек подразделений в файл формата XLSX\".%s",
                throwable != null ? " Причина: " + throwable.getMessage() : "");
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return "Выгрузка настроек подразделений в файл формата XLSX";
    }
}
