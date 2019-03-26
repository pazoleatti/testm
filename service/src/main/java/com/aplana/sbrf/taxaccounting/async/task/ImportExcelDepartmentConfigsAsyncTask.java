package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Загрузка excel-файла с настройками подразделений
 */
@Component("ImportExcelDepartmentConfigsAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
        long blobLength = params.containsKey("fileSize") ? (Long) params.get("fileSize") : 0;
        long fileSize = (long) Math.ceil(blobLength / 1024.);
        String msg = String.format("размер файла (%s Кбайт) превышает максимально допустимый (%s Кбайт).", fileSize, "%s");
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
}
