package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;
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
    TAUserService userService;

    @Autowired
    PrintingService printingService;

    @Override
    protected void executeBusinessLogic(Map<String, Object> params) {
        String lock = (String) params.get(LOCKED_OBJECT.name());
        int userId = (Integer) params.get(USER_ID.name());
        TAUser userInfo = userService.getUser(userId);
        //printingService.generateCSV(userInfo, (Long)params.get("formDataId"), (Boolean)params.get("manual"), (Boolean)params.get("isShowChecked"));
        //TODO
    }

    @Override
    protected String getAsyncTaskName() {
        return "Генерация xlsm-файла";
    }
}
