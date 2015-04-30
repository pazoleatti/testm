package com.aplana.sbrf.taxaccounting.async.manager;

import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;

import java.util.Map;

/**
 * Класс, выполняющий логику асинхронной задачи в отдельном потоке
 * @author dloshkarev
 */
public class AsyncTaskThread implements Runnable {
    private final Map<String, Object> params;
    private final AsyncTask task;

    public AsyncTaskThread(AsyncTask task, Map<String, Object> params) {
        this.task = task;
        this.params = params;
    }

    @Override
    public void run() {
        task.execute(params);
    }
}
