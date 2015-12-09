package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.IfrsDataService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCK_DATE;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class IfrsGeneratorAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private IfrsDataService ifrsDataService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private LockDataService lockService;

    @Override
    protected ReportType getReportType() {
        return ReportType.ZIP_IFRS;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) {
        return BalancingVariants.LONG;
    }

    @Override
    protected boolean executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        Integer reportPeriod = (Integer)params.get("reportPeriodId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        ifrsDataService.calculate(logger, reportPeriod, new LockStateLogger() {
            @Override
            public void updateState(String state) {
                lockService.updateState(lock, lockDate, state);
            }
        });
        return true;
    }

    @Override
    protected String getAsyncTaskName() {
        return "Формирование отчетности для МСФО";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        Integer reportPeriodId = (Integer)params.get("reportPeriodId");
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
        return String.format("Сформирован архив с отчетностью для МСФО за %s %s", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        Integer reportPeriodId = (Integer)params.get("reportPeriodId");
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
        return String.format("Произошла непредвиденная ошибка при формировании архива с отчетностью для МСФО за %s %s. Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
    }
}
