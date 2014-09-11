package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.Map;

@Local(AsyncTaskLocal.class)
@Remote(AsyncTaskRemote.class)
@Stateless
@Interceptors(AsyncTaskInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class XmlGeneratorAsyncTask extends AbstractAsyncTask {

    @Override
    protected void executeBusinessLogic(Map<String, Object> params) {
        //TODO
    }

    @Override
    protected String getAsyncTaskName() {
        return "Генерация xml-файла";
    }
}
