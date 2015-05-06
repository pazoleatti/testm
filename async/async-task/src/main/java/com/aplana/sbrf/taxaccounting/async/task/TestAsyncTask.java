package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * Проверочный таск
 * @author dloshkarev
 */
@Local(AsyncTaskLocal.class)
@Remote(AsyncTaskRemote.class)
@Stateless
@Interceptors(AsyncTaskInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TestAsyncTask extends AbstractAsyncTask {

    @Autowired
    TAUserService userService;

    @Autowired
    RefBookFactory refBookFactory;

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) throws InterruptedException {
        /*while (true) {
            System.out.println("TestAsyncTask started: " + new Date().getTime());
            Thread.sleep(1000);
        }*/
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
    protected String getErrorMsg(Map<String, Object> params) {
        return "Ошибка в тестовой задаче";
    }
}
