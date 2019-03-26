package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Идентификация налоговой формы
 */
@Component("IdentifyAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IdentifyAsyncTask extends XmlGeneratorAsyncTask {

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.IDENTIFY_PERSON;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
            Date docDate = (Date) taskData.getParams().get("docDate");
            long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
            TAUserInfo userInfo = new TAUserInfo();
            userInfo.setUser(userService.getUser(taskData.getUserId()));
            Map<String, Object> exchangeParams = new HashMap<>();
            exchangeParams.put("taskDataId", taskData.getId());
            declarationDataService.identify(new TargetIdAndLogger(declarationDataId, logger), userInfo, docDate, exchangeParams, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            });
            if (logger.containsLevel(LogLevel.ERROR)) {
                return new BusinessLogicResult(false, null);
            }
            return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        return String.format("Операция \"Идентификация ФЛ\" выполнена %s.",
                getDeclarationFullName(declarationDataId));
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        return String.format("Не выполнена операция \"Идентификация ФЛ\" %s. Причина: %s.",
                getDeclarationFullName(declarationDataId),
                taskData.getParams().get("errorsText"));
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return String.format(getAsyncTaskType().getDescription(),
                getDeclarationFullName(declarationDataId));
    }

    /**
     * Получение полной информации о форме (номер, период, подразделение)
     *
     * @param declarationDataId идентификатор формы
     * @return строка с информацией о форме
     */
    private String getDeclarationFullName(long declarationDataId){
        DeclarationData declarationData = declarationDataService.get(declarationDataId, null);
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        return String.format("для налоговой формы: № %s, Период: \"%s, %s %s\", Подразделение: \"%s\"",
                declarationDataId,
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                reportPeriod.getCorrectionDate() != null ? String.format("корр. %s", new SimpleDateFormat("dd.MM.yyyy").format(reportPeriod.getCorrectionDate())) : "",
                department.getName());
    }

    /**
     * Проверка успешности выполнения асинка
     *
     * @param taskData проверяемая задача
     * @return признак успеха выполнения задачи
     */
    private boolean isSuccess(AsyncTaskData taskData) {
        return taskData.getParams().get("errorsText") == null;
    }

}
