package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.service.ServerInfo;
import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс-контейнер для запуска потоков обработки асинхронных задач
 *
 * @author dloshkarev
 */
@Component
public class AsyncTaskThreadContainer {
    private static final Log LOG = LogFactory.getLog(AsyncTaskThreadContainer.class);
    // Таймаут, после которого задача считается упавшей и ее выполнение надо передать другому узлу (ч)
    private static final int TASK_TIMEOUT = 10;

    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private ServerInfo serverInfo;
    @Autowired
    private TAUserService taUserService;
    @Autowired
    private ApplicationInfo applicationInfo;
    @Autowired
    private ExecutorService asyncTaskExecutorService;

    /**
     * Метод запускает потоки обработки асинхронных задач для каждой очереди
     */
    public void processQueues() {
        new AsyncTaskShortQueueProcessor().processQueue();
        new AsyncTaskLongQueueProcessor().processQueue();
    }

    /**
     * Класс управляющий обработкой асинхронных задач.
     * Процесс выполнения асинхронной задачи состоит из этапов:
     * - Мониторинг БД на наличие незарезервированных задач
     * - Резервирование задачи на текущий узел кластера
     * - Запуск выполнения задачи
     */
    private abstract class AbstractQueueProcessor {

        /**
         * Возвращает следующую в очереди задачу на выполнение
         *
         * @return данные задачи
         */
        protected abstract AsyncTaskData getNextTask();

        /**
         * Возвращает количество потоков, обрабатывающих задачи из очереди
         *
         * @return количество потоков
         */
        protected abstract int getThreadCount();

        /**
         * Запускает обработку задач в очереди, каждая задача выполняется в отдельном потоке
         */
        public void processQueue() {
            for (int i = 0; i < getThreadCount(); i++) {
                //Получаем первую в списке незарезирвированную задачу и резервируем ее под текущий узел
                final AsyncTaskData taskData = getNextTask();
                if (taskData == null) {
                    //Новых задач нет, либо текущий узел занят
                    return;
                }
                try {
                    //Запускаем выполнение бина-обработчика задачи в новом потоке
                    LOG.info("Task started: " + taskData);
                    final AsyncTask task = asyncManager.getAsyncTaskBean(taskData.getType().getAsyncTaskTypeId());
                    asyncTaskExecutorService.submit(new Thread() {
                        @Override
                        public void run() {
                            Thread.currentThread().setName("AsyncTask-" + taskData.getId());

                            try {
                                //Запускаем задачу под нужным пользователем
                                TAUser user = taUserService.getUser(taskData.getUserId());
                                List<String> roles = new ArrayList<String>();
                                for (TARole role : user.getRoles()) {
                                    roles.add(role.getAlias());
                                }
                                Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(roles.toArray(new String[roles.size()]));
                                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                        new User(user.getLogin(), "user", authorities),
                                        user.getLogin(),
                                        authorities
                                );
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                task.execute(taskData);
                            } catch (Exception e) {
                                LOG.error("Unexpected error during async task execution", e);
                            } finally {
                                asyncManager.finishTask(taskData.getId());
                            }
                        }
                    });
                    Thread.sleep(500);
                } catch (Exception e) {
                    LOG.info("Unexpected error during startup async task execution", e);
                    asyncManager.finishTask(taskData.getId());
                }
            }
        }
    }

    /**
     * Класс управляющий обработкой очереди коротких асинхронных задач.
     */
    private final class AsyncTaskShortQueueProcessor extends AbstractQueueProcessor {
        @Override
        protected AsyncTaskData getNextTask() {
            String priorityNode = applicationInfo.isProductionMode() ? null : serverInfo.getServerName();
            return asyncManager.reserveTask(serverInfo.getServerName(), priorityNode, TASK_TIMEOUT, AsyncQueue.SHORT, getThreadCount());
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
        protected AsyncTaskData getNextTask() {
            String priorityNode = applicationInfo.isProductionMode() ? null : serverInfo.getServerName();
            return asyncManager.reserveTask(serverInfo.getServerName(), priorityNode, TASK_TIMEOUT, AsyncQueue.LONG, getThreadCount());
        }

        @Override
        protected int getThreadCount() {
            return 3;
        }
    }
}
