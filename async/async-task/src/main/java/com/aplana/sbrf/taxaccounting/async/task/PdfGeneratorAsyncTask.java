package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.Map;

@Local(AsyncTaskLocal.class)
@Remote(AsyncTaskRemote.class)
@Stateless
@Interceptors(AsyncTaskInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PdfGeneratorAsyncTask extends AbstractAsyncTask {

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        //TODO
    }

    @Override
    protected String getAsyncTaskName() {
        return "Генерация pdf-файла";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        //TODO
        return null;
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        return null;
    }
}
