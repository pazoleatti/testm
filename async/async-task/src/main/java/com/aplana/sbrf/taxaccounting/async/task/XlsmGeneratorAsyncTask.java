package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

@Local(AsyncTaskLocal.class)
@Remote(AsyncTaskRemote.class)
@Stateless
@Interceptors(AsyncTaskInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class XlsmGeneratorAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private PrintingService printingService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private ReportService reportService;

    @Override
    protected void executeBusinessLogic(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        boolean isShowChecked = (Boolean)params.get("isShowChecked");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);

        String uuid = printingService.generateExcel(userInfo, formDataId, manual, isShowChecked);

        Notification notification = new Notification();
        notification.setCreateDate(new Date());
        notification.setDeadline(new Date(115, 1, 1));
        notification.setReportPeriodId(formData.getReportPeriodId());
        notification.setReceiverDepartmentId(formData.getDepartmentId());
        notification.setSenderDepartmentId(userService.getUser(userId).getDepartmentId());
        notification.setText(params.toString());
        reportService.create(formDataId, uuid, ReportType.EXCEL, isShowChecked, manual, false);
        //notificationService.save(notification);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Генерация xlsm-файла";
    }
}
