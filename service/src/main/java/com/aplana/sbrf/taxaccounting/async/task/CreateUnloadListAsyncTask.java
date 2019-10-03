package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Выгрузка списка источники-приемники в файл xlsx
 */
@Component("CreateUnloadListAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CreateUnloadListAsyncTask extends AbstractAsyncTask {
    @Autowired
    private PersonService personService;
    @Autowired
    private PrintingService printingService;
    @Autowired
    private SourceService sourceService;

    @Autowired

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.EXCEL_UNLOAD_LIST;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        Long declarationDataId = (Long) params.get("declarationDataId");
        Boolean sources = (Boolean) params.get("sources");
        Boolean destinations = (Boolean) params.get("destinations");
        List<Relation> relationList = new ArrayList<>();
        if (sources) relationList.addAll(sourceService.getDeclarationSourcesInfo(declarationDataId));
        if (destinations) relationList.addAll(sourceService.getDeclarationDestinationsInfo(declarationDataId));
        long value = (int) relationList.size();
        if (value == 0) {
            throw new ServiceException("Выполнение операции \"%s\" невозможно, т.к. по заданным параметрам не найдено ни одной записи", taskDescription);
        }
        String msg = String.format("количество отобранных для выгрузки в файл записей (%s) больше, чем разрешенное значение (%s).", value, "%s");
        return checkTask(value, taskDescription, msg);
    }

    @Override
    protected AsyncQueue checkTask(Long value, String taskName, String msg) throws AsyncTaskException {
        AsyncTaskTypeData taskTypeData = asyncTaskTypeDao.findById(getAsyncTaskType().getId());
        if (taskTypeData == null) {
            throw new AsyncTaskException(String.format("Cannot find task parameters for \"%s\"", taskName));
        }
        Long taskLimit = taskTypeData.getTaskLimit();
        if (taskLimit != null && taskLimit != 0 && value != 0 && taskLimit < value) {
            String errorText = "Количество отобранных для выгрузки в файл записей превышает пороговое значение = "
                    + taskLimit + " строк";
            throw new ServiceException(errorText, taskName);
        } else {
            return AsyncQueue.SHORT;
        }
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        Long declarationDataId = (Long) params.get("declarationDataId");
        Boolean sources = (Boolean) params.get("sources");
        Boolean destinations = (Boolean) params.get("destinations");
        TAUser user = userService.getUser(taskData.getUserId());
        String uuid = printingService.generateExcelUnloadList(declarationDataId, sources, destinations, user);
        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return "Завершена операция \"Выгрузка списка источники-приемники в XLSX-формате\"";
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        Throwable throwable = (Throwable) taskData.getParams().get("exceptionThrown");
        return String.format("Не выполнена операция \"Выгрузка списка источники-приемники в XLSX-формате\".%s",
                throwable != null ? " Причина: " + throwable.getMessage() : "");
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return "Выгрузка списка источники-приемники в XLSX-формате";
    }

}
