package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Map;


@Component("CreateReportsAsyncTask")
public class CreateReportsAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationTypeService declarationTypeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private AsyncManager asyncManager;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.CREATE_REPORTS_DEC;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.LONG;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        Integer declarationTypeId = (Integer) params.get("declarationTypeId");
        Integer departmentReportPeriodId = (Integer) params.get("departmentReportPeriodId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(departmentReportPeriodId);

        if (departmentReportPeriod == null) {
            throw new ServiceException("Не удалось определить налоговый период.");
        }

        String uuid = declarationDataService.createReports(logger, userInfo, departmentReportPeriod, declarationTypeId, new LockStateLogger() {
            @Override
            public void updateState(AsyncTaskState state) {
                asyncManager.updateState(taskData.getId(), state);
            }
        });

        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        Integer declarationTypeId = (Integer) taskData.getParams().get("declarationTypeId");
        Integer departmentReportPeriodId = (Integer) taskData.getParams().get("departmentReportPeriodId");

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(departmentReportPeriodId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId, departmentReportPeriod.getReportPeriod().getId()));

        String strCorrPeriod = "";
        if (departmentReportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.get().format(departmentReportPeriod.getCorrectionDate());
        }

        return String.format("Подготовлена к выгрузке отчетность \"%s\": Период: \"%s, %s%s\", Подразделение: \"%s\"",
                declarationTemplate.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(), departmentReportPeriod.getReportPeriod().getName(), strCorrPeriod,
                department.getName());
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        Integer declarationTypeId = (Integer) taskData.getParams().get("declarationTypeId");
        Integer departmentReportPeriodId = (Integer) taskData.getParams().get("departmentReportPeriodId");

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(departmentReportPeriodId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId, departmentReportPeriod.getReportPeriod().getId()));

        String strCorrPeriod = "";
        if (departmentReportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.get().format(departmentReportPeriod.getCorrectionDate());
        }

        return String.format("Произошла непредвиденная ошибка при формирование отчетности форм \"%s\": Период: \"%s, %s%s\", Подразделение: \"%s\"",
                declarationTemplate.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(), departmentReportPeriod.getReportPeriod().getName(), strCorrPeriod,
                department.getName());
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return String.format(getAsyncTaskType().getDescription(),
                declarationDataService.getDeclarationFullName(declarationDataId, DeclarationDataReportType.getDDReportTypeByReportType(getAsyncTaskType())));
    }
}
