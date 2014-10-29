package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

/**
 * Спринговая реализация таска "Генерация xlsm-файл" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("TestIfrsGeneratorAsyncTaskSpring")
@Transactional
public class TestIfrsGeneratorAsyncTaskSpring extends AbstractAsyncTask {

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
        Integer reportPeriodId = (Integer)params.get("reportPeriodId");
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
        return String.format("Произошла непредвиденная ошибка при формировании архива с отчетностью для МСФО за %s %s. Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
    }
}
