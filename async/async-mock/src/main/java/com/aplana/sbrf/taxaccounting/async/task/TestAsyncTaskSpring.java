package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Спринговая реализация тестового таска для вызова из дев-мода
 * @author dloshkarev
 */
@Component("TestAsyncTaskSpring")
public class TestAsyncTaskSpring extends TestAsyncTask {

    @Override
    protected String getAsyncTaskName() {
        return "Тестовая асинхронная задача для dev-mode";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return "Тест тест тест dev-mode";
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params, boolean unexpected) {
        return "Dev-мод ошибка";
    }
}
