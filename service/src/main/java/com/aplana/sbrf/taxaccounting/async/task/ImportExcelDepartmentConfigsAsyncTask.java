package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Загрузка excel-файла с настройками подразделений
 */
@Component("ImportExcelDepartmentConfigsAsyncTask")
public class ImportExcelDepartmentConfigsAsyncTask extends AbstractAsyncTask {

    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private DepartmentConfigService departmentConfigService;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.IMPORT_DEPARTMENT_CONFIGS;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        long blobLength = blobDataService.getLength((String) params.get("blobDataId"));
        long fileSize = (long) Math.ceil(blobLength / 1024.);
        String msg = String.format("Размер файла(%s) превышает максимально допустимый(%s)!", fileSize, "%s");
        return checkTask(fileSize, taskDescription, msg);
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        BlobData blobData = blobDataService.get((String) params.get("blobDataId"));
        int departmentId = (Integer) params.get("departmentId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        departmentConfigService.importExcel(departmentId, blobData, userInfo, logger);
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return "Загружены настройки подразделений из файла \"" + taskData.getParams().get("fileName") + "\".";
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return "Ошибка загрузки файла \"" + taskData.getParams().get("fileName")
                + "\" настроек подразделений. Обратитесь к Администратору Системы или повторите операцию позднее.";
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return "Загрузка файла настроек подразделений \"" + params.get("fileName") + "\".";
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
