package com.aplana.sbrf.taxaccounting.async.task.impl;

import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTaskLocal;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTaskRemote;
import com.aplana.sbrf.taxaccounting.async.task.UploadTransportDataAsyncTask;

import javax.ejb.*;
import javax.interceptor.Interceptors;

/**
 * Реализация таска "Загрузка ТФ с локального компьютера"
 * @author Lhaziev
 */
@Local(AsyncTaskLocal.class)
@Remote(AsyncTaskRemote.class)
@Stateless
@Interceptors(AsyncTaskInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UploadTransportDataAsyncTaskImpl extends UploadTransportDataAsyncTask {
}