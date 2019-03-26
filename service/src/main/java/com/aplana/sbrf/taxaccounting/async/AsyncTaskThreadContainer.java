package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.ServerInfo;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Класс-контейнер для запуска потоков обработки асинхронных задач
 *
 * @author dloshkarev
 */
@Component
public class AsyncTaskThreadContainer implements DisposableBean {
    private static final Log LOG = LogFactory.getLog(AsyncTaskThreadContainer.class);
    // Таймаут, после которого задача считается упавшей и ее выполнение надо передать другому узлу (ч)
    private static final int TASK_TIMEOUT_HOURS = 10;

    private AsyncManager asyncManager;
    private ServerInfo serverInfo;
    private TAUserService taUserService;
    private ApplicationInfo applicationInfo;
    private ExecutorService asyncTaskPool;
    private ExecutorService asyncTaskMonitorPool;

    public AsyncTaskThreadContainer(AsyncManager asyncManager, ServerInfo serverInfo, TAUserService taUserService,
                                    ApplicationInfo applicationInfo) {
        this.asyncManager = asyncManager;
        this.serverInfo = serverInfo;
        this.taUserService = taUserService;
        this.applicationInfo = applicationInfo;

        ThreadFactory asyncTaskPoolNamedThreadFactory = new ThreadFactoryBuilder().setNameFormat("async_task_pool-thread-%d").build();
        this.asyncTaskPool = Executors.newCachedThreadPool(asyncTaskPoolNamedThreadFactory);

        ThreadFactory asyncTaskMonitorPoolNamedThreadFactory = new ThreadFactoryBuilder().setNameFormat("async_task_monitor_pool-thread-%d").build();
        this.asyncTaskMonitorPool = Executors.newCachedThreadPool(asyncTaskMonitorPoolNamedThreadFactory);
    }

    /**
     * Метод запускает потоки обработки асинхронных задач для каждой очереди
     */
    public void processQueues() {
        new QueueProcessor(AsyncQueue.SHORT, 3).processQueue();
        new QueueProcessor(AsyncQueue.LONG, 3).processQueue();
    }

    @Override
    public void destroy() {
        LOG.info("Shutdown ...");
        asyncTaskMonitorPool.shutdownNow();
        asyncTaskPool.shutdownNow();
    }

    /**
     * Класс управляющий обработкой асинхронных задач.
     * Процесс выполнения асинхронной задачи состоит из этапов:
     * - Мониторинг БД на наличие незарезервированных задач
     * - Резервирование задачи на текущий узел кластера
     * - Запуск выполнения задачи
     */
    private final class QueueProcessor {
        // Очередь
        private final AsyncQueue asyncQueue;

        // Количество потоков, обрабатывающих задачи из очереди
        private final int threadCount;

        private QueueProcessor(AsyncQueue asyncQueue, int threadCount) {
            this.asyncQueue = asyncQueue;
            this.threadCount = threadCount;
        }

        /**
         * Возвращает следующую в очереди задачу на выполнение
         *
         * @return данные задачи
         */
        private AsyncTaskData getNextTask() {
            String priorityNode = applicationInfo.isProductionMode() ? null : serverInfo.getServerName();
            return asyncManager.reserveTask(serverInfo.getServerName(), priorityNode, TASK_TIMEOUT_HOURS, asyncQueue, threadCount);
        }

        /**
         * Запускает обработку задач в очереди, каждая задача выполняется в отдельном потоке
         */
        public void processQueue() {
            for (int i = 0; i < threadCount; i++) {
                //Получаем первую в списке незарезирвированную задачу и резервируем ее под текущий узел
                final AsyncTaskData taskData = getNextTask();
                if (taskData == null) {
                    //Новых задач нет, либо текущий узел занят
                    return;
                }
                try {
                    Future<?> taskFuture = asyncTaskPool.submit(new Thread("AsyncTask-" + taskData.getId()) {
                        @Override
                        public void run() {
                            try {
                                if (asyncManager.isTaskActive(taskData.getId())) {
                                    MDC.put("processId", String.format("AsyncTaskId=%s ", taskData.getId()));
                                    //Запускаем выполнение бина-обработчика задачи в новом потоке
                                    LOG.info("Task started: " + taskData);
                                    final AsyncTask task = asyncManager.getAsyncTaskBean(taskData.getType().getAsyncTaskTypeId());
                                    final Map<String, String> mdcContext = MDC.getCopyOfContextMap();
                                    MDC.setContextMap(mdcContext);
                                    //Запускаем задачу под нужным пользователем
                                    TAUser user = taUserService.getUser(taskData.getUserId());
                                    MDC.put("userInfo", String.format("%s ", user.getLogin()));
                                    //Формируем и передаём в контекст спринга параметры аутентификации (пользователь и его роли)
                                    //чтобы корректно работала security в данном потоке
                                    List<String> roles = new ArrayList<>();
                                    for (TARole role : user.getRoles()) {
                                        roles.add(role.getAlias());
                                    }
                                    Collection<GrantedAuthority> authorities;
                                    authorities = AuthorityUtils.createAuthorityList(roles.toArray(new String[0]));
                                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                                            new User(user.getLogin(), "user", authorities),
                                            user.getLogin(),
                                            authorities
                                    );
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                    task.execute(taskData);
                                }
                            } catch (Exception e) {
                                LOG.error("Unexpected error during async task execution", e);
                            } finally {
                                MDC.clear();
                                asyncManager.finishTask(taskData.getId());
                            }
                        }
                    });

                    //Запускаем мониторинг состояния задачи для ее остановки в случае отмены.
                    asyncTaskMonitorPool.submit(new TaskStateMonitor(taskFuture, taskData.getId()));
                    Thread.sleep(500);
                } catch (Exception e) {
                    LOG.info("Unexpected error during startup async task execution", e);
                    asyncManager.finishTask(taskData.getId());
                } finally {
                    MDC.clear();
                }
            }
        }

        /**
         * Класс отвечающий за мониторинг состояния выполнения задачи, если задача была удалена или перешла в состояние CANCELLED, то поток, занимающийся ее выполнением прерывается
         */
        private final class TaskStateMonitor implements Runnable {
            private Future<?> taskFuture;
            private long taskId;
            private boolean canceled = false;

            TaskStateMonitor(Future<?> taskFuture, long taskId) {
                this.taskFuture = taskFuture;
                this.taskId = taskId;
            }

            @Override
            public void run() {
                while (!canceled) {
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        //Do nothing
                    }
                    if (!asyncManager.isTaskActive(taskId)) {
                        canceled = true;
                        asyncManager.finishTask(taskId);
                        taskFuture.cancel(true);
                        LOG.info(String.format("Async task with id %s was cancelled", taskId));
                    }
                }
            }
        }
    }
}