package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.io.ByteArrayInputStream;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

@Local(AsyncTaskLocal.class)
@Remote(AsyncTaskRemote.class)
@Stateless
@Interceptors(AsyncTaskInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class XlsxGeneratorAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Override
    protected void executeBusinessLogic(Map<String, Object> params) {
        long declarationDataId = (Long)params.get("declarationDataId");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        reportService.createDec(declarationDataId, blobDataService.create(new ByteArrayInputStream(declarationDataService.getXlsxData(declarationDataId, userInfo)), ""), ReportType.EXCEL_DEC);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Генерация xlsx-файла";
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
        return String.format("Сформирован %s отчет декларации: Период: \"%s, %s\", Подразделение: \"%s\", Вид: \"%s\".", ReportType.EXCEL.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(),
                declarationTemplate.getType().getName());
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        return null;
    }
}
