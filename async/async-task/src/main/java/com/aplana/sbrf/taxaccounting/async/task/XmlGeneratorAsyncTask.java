package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class XmlGeneratorAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) {
        return BalancingVariants.LONG;
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        log.debug("XmlGeneratorAsyncTaskImpl has been started");
        Date docDate = (Date)params.get("docDate");
        long declarationDataId = (Long)params.get("declarationDataId");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        declarationDataService.calculate(logger, declarationDataId, userInfo, docDate);
        log.debug("XmlGeneratorAsyncTaskImpl has been finished");
    }

    @Override
    protected String getAsyncTaskName() {
        return "Генерация xml-файла";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long declarationDataId = (Long)params.get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        DeclarationData declaration = declarationDataService.get(declarationDataId, userInfo);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        String str;
        if (TaxType.PROPERTY.equals(declarationTemplate.getType().getTaxType()) || TaxType.TRANSPORT.equals(declarationTemplate.getType().getTaxType())) {
            str = String.format(", Налоговый орган: \"%s\", КПП: \"%s\".", declaration.getTaxOrganCode(), declaration.getKpp());
        } else {
            str = ".";
        }
        return String.format("Сформирован %s отчет декларации: Период: \"%s, %s\", Подразделение: \"%s\", Вид: \"%s\"%s",
                ReportType.XML_DEC.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(),
                declarationTemplate.getType().getName(), str);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long declarationDataId = (Long)params.get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        DeclarationData declaration = declarationDataService.get(declarationDataId, userInfo);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        String str;
        if (TaxType.PROPERTY.equals(declarationTemplate.getType().getTaxType()) || TaxType.TRANSPORT.equals(declarationTemplate.getType().getTaxType())) {
            str = String.format(", Налоговый орган: \"%s\", КПП: \"%s\".", declaration.getTaxOrganCode(), declaration.getKpp());
        } else {
            str = ".";
        }
        return String.format("Произошла непредвиденная ошибка при формировании %s отчета декларации: Период: \"%s, %s\", Подразделение: \"%s\", Вид: \"%s\"%s Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета",
                ReportType.XML_DEC.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(),
                declarationTemplate.getType().getName(), str);
    }
}
