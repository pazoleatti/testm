package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.IfrsDataService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

@Local(AsyncTaskLocal.class)
@Remote(AsyncTaskRemote.class)
@Stateless
@Interceptors(AsyncTaskInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class IfrsGeneratorAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private IfrsDataService ifrsDataService;

    @Autowired
    private PeriodService periodService;

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        Integer reportPeriod = (Integer)params.get("reportPeriodId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        ifrsDataService.calculate(logger, reportPeriod);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Генерация отчетности для МСФО";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        Integer reportPeriodId = (Integer)params.get("reportPeriodId");
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
        return String.format("Сформирован архив с отчетностью для МСФО за %s %s", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        return "";
    }
}
