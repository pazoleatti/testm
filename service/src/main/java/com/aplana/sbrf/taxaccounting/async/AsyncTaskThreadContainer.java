package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс-контейнер для запуска потоков обработки асинхронных задач
 *
 * @author dloshkarev
 */
@Component
public class AsyncTaskThreadContainer {
    private static final Log LOG = LogFactory.getLog(AsyncTaskShortQueueProcessor.class);
    // Таймаут, после которого задача считается упавшей и ее выполнение надо передать другому узлу (ч)
    private static final int TASK_TIMEOUT = 10;

    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private AsyncTaskDao asyncTaskDao;
    @Autowired
    private ServerInfo serverInfo;

    /**
     * Метод запускает потоки обработки асинхронных задач для каждой очереди
     */
    public void processQueues() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        DelegatingSecurityContextExecutorService executor = new DelegatingSecurityContextExecutorService(executorService, SecurityContextHolder.getContext());
        executor.submit(new AsyncTaskShortQueueProcessor());
        executor.submit(new AsyncTaskLongQueueProcessor());
        executor.shutdown();
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
            for (int i = 0; i < getThreadCount(); i++) {
                //Получаем первую в списке незарезирвированную задачу и резервируем ее под текущий узел
                final AsyncTaskData taskData = getNextTask(TASK_TIMEOUT);
                try {
                    if (taskData != null) {
                        //Запускаем выполнение бина-обработчика задачи в новом потоке
                        LOG.info("Task started: " + taskData);
                        final AsyncTask task = asyncManager.getAsyncTaskBean(taskData.getTypeId());
                        executorService.submit(new Thread() {
                            @Override
                            public void run() {
                                Thread.currentThread().setName("AsyncTask-" + taskData.getId());
                                try {
                                    task.execute(taskData.getParams());
                                } catch (Exception e) {
                                    LOG.error("Unexpected error during async task execution", e);
                                } finally {
                                    asyncTaskDao.finishTask(taskData);
                                }
                            }
                        });
                    }
                    Thread.sleep(500);
                } catch (Exception e) {
                    LOG.info("Unexpected error during startup async task execution", e);
                    asyncTaskDao.finishTask(taskData);
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
            return asyncManager.reserveTask(serverInfo.getServerName(), taskTimeout, BalancingVariants.SHORT, getThreadCount());
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
            return asyncManager.reserveTask(serverInfo.getServerName(), taskTimeout, BalancingVariants.LONG, getThreadCount());
        }

        @Override
        protected int getThreadCount() {
            return 1;
        }
    }
}
