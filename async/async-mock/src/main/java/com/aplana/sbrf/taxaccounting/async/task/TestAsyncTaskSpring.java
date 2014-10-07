package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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

    @Autowired
    RefBookFactory refBookFactory;

    @Override
    protected void executeBusinessLogic(Map<String, Object> params) {
        System.out.println("TestAsyncTaskSpring has been started!");
        /*TAUserInfo userInfo = (TAUserInfo) params.get("userInfo");
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        refBookFactory.getDataProvider(13L).deleteRecordVersions(logger, Arrays.asList(274873099L));*/
    }

    @Override
    protected String getAsyncTaskName() {
        return "Тестовая асинхронная задача для dev-mode";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return "Тест тест тест dev-mode";
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        return "Dev-мод ошибка";
    }
}
