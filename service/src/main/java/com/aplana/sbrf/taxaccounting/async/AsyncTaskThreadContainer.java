package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Класс-контейнер для запуска потоков обработки асинхронных задач
 *
 * @author dloshkarev
 */
@Component
public class AsyncTaskThreadContainer {
    private static final Log LOG = LogFactory.getLog(AsyncTaskShortQueueProcessor.class);
    // Таймаут на опрос очереди задач (мс)
    private static final int QUEUE_MONITORING_TIMEOUT = 5000;
    // Таймаут, после которого задача считается упавшей и ее выполнение надо передать другому узлу (ч)
    private static final int TASK_TIMEOUT = 10;

    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private AsyncTaskDao asyncTaskDao;
    @Autowired
    private ServerInfo serverInfo;

    private ExecutorService executorService;

    /**
     * Метод вызывается на старте приложения и запускает потоки для мониторинга и обработки очереди асинхронных задач
     */
    @PostConstruct
    public void onStartup() {
        executorService = Executors.newFixedThreadPool(2);
        executorService.submit(new AsyncTaskShortQueueProcessor());
        executorService.submit(new AsyncTaskLongQueueProcessor());
    }

    @PreDestroy
    private void destroy() {
        if (!executorService.isShutdown() && !executorService.isTerminated()) {
            executorService.shutdownNow();
        }
    }

    /**
     * Класс управляющий обработкой асинхронных задач.
     * Процесс выполнения асинхронной задачи состоит из этапов:
     * - Мониторинг БД на наличие незарезервированных задач
     * - Резервирование задачи на текущий узел кластера
     * - Запуск выполнения задачи
     */
    private abstract class AbstractQueueProcessor implements Runnable {

        /**
         * Возвращает следующую в очереди задачу на выполнение
         *
         * @param taskTimeout таймаут на выполнение задачи, после окончания которого считается, что узел ее выполняющий упал и надо передать выполнение другому узлу
         * @return данные задачи
         */
        protected abstract AsyncTaskData getNextTask(int taskTimeout);

        /**
         * Возвращает количество потоков, обрабатывающих задачи из очереди
         *
         * @return количество потоков
         */
        protected abstract int getThreadCount();

        @Override
        public void run() {
            final ExecutorService executorService = Executors.newFixedThreadPool(getThreadCount());
            while (true) {
                try {
                    //Получаем первую в списке незарезирвированную задачу и резервируем ее под текущий узел
                    final AsyncTaskData taskData = getNextTask(TASK_TIMEOUT);
                    if (taskData != null) {
                        //Запускаем выполнение бина-обработчика задачи в новом потоке
                        LOG.debug("Запускается задача: " + taskData);
                        final AsyncTask task = asyncManager.getAsyncTaskBean(taskData.getTypeId());
                        executorService.submit(new Thread() {
                            @Override
                            public void run() {
                                Thread.currentThread().setName("AsyncTask-" + taskData.getId());
                                task.execute(taskData.getParams());
                                asyncTaskDao.finishTask(taskData.getId());
                            }
                        });
                    } else {
                        Thread.sleep(QUEUE_MONITORING_TIMEOUT);
                    }
                } catch (AsyncTaskException e) {
                    LOG.error("Ошибка получения бина-обработчика асинхронной задачи", e);
                } catch (Exception e) {
                    LOG.error("Непредвиденная ошибка во время выполнения асинхронной задачи", e);
                } finally {
                    try {
                        Thread.sleep(QUEUE_MONITORING_TIMEOUT);
                    } catch (InterruptedException e1) {
                        //do nothing
                    }
                }
            }
        }
    }

    /**
     * Класс управляющий обработкой очереди коротких асинхронных задач.
     */
    private final class AsyncTaskShortQueueProcessor extends AbstractQueueProcessor {
        @Override
        protected AsyncTaskData getNextTask(int taskTimeout) {
            return asyncTaskDao.reserveTask(serverInfo.getServerName(), taskTimeout, BalancingVariants.SHORT, getThreadCount());
        }

        @Override
        protected int getThreadCount() {
            return 3;
        }
    }

    /**
     * Класс управляющий обработкой очереди коротких асинхронных задач.
     */
    private final class AsyncTaskLongQueueProcessor extends AbstractQueueProcessor {
        @Override
        protected AsyncTaskData getNextTask(int taskTimeout) {
            return asyncTaskDao.reserveTask(serverInfo.getServerName(), taskTimeout, BalancingVariants.LONG, getThreadCount());
        }

        @Override
        protected int getThreadCount() {
            return 1;
        }
    }
}
