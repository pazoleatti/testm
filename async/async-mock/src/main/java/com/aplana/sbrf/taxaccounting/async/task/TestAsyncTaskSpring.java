package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Спринговая реализация тестового таска для вызова из дев-мода
 * @author dloshkarev
 */
@Component("TestAsyncTaskSpring")
public class TestAsyncTaskSpring extends AbstractAsyncTask {

    // private final Log log = LogFactory.getLog(getClass());

    @Autowired
    TAUserService userService;

    @Override
    protected void executeBusinessLogic(Map<String, Object> params) {
        System.out.println("TestAsyncTaskSpring has been started!");
        System.out.println("params: " + params);
        System.out.println("admin: " + userService.getUser("admin").getName());
    }

    @Override
    protected String getAsyncTaskName() {
        return "Тестовая асинхронная задача для dev-mode";
    }

    @Override
    protected String getNotificationMsg() {
        return "Тест тест тест dev-mode";
    }
}
