package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Спринговая реализация тестового таска для вызова из дев-мода
 * @author dloshkarev
 */
@Component("TestAsyncTaskSpring")
public class TestAsyncTaskSpring implements AsyncTask {

    private final Log LOG = LogFactory.getLog(getClass());

    @Autowired
    TAUserService userService;

    @Override
    public void execute(Map<String, Object> params) {
        System.out.println("TestAsyncTaskSpring has been started!");
        System.out.println("params: " + params);
        System.out.println("admin: " + userService.getUser("admin").getName());
    }
}
