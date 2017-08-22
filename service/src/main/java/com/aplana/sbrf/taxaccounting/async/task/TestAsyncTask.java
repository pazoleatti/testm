package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Проверочный таск
 * @author dloshkarev
 */
@Component
public class TestAsyncTask extends AbstractAsyncTask {

    @Autowired
    TAUserService userService;

    @Autowired
    RefBookFactory refBookFactory;

    @Override
    protected ReportType getReportType() {
        return null;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) {
        return BalancingVariants.SHORT;
    }

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) throws InterruptedException {
        for (int i = 0; i < 20; i ++) {
            System.out.println("TestAsyncTaskImpl started: " + new Date().getTime() + ". Thread: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        return new TaskStatus(true, null);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Тестовая задача";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return "Тест тест тест!";
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params, boolean unexpected) {
        return "Ошибка в тестовой задаче";
    }
}
