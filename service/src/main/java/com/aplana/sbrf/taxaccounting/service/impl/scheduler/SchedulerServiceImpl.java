package com.aplana.sbrf.taxaccounting.service.impl.scheduler;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTaskThreadContainer;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DBToolService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.SchedulerTaskService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Класс для запуска периодических задач по расписанию
 * Является бином, чтобы не надо было создавать его экземпляр каждый раз при вызове методов и обеспечить его существование в единственном экземпляре
 * Обрабатываются методы указанные как executor в списке executors.
 * Планируется, что нужные сервисы будут вызываться уже из этого класса, это необходимо, т.к при получении методов через рефлексию нельзя получить экземпляр этого класса в спринговом конфиге,
 * а класс Scheduler уже является бином и соответственно ему доступны все остальные бины.
 *
 * @author dloshkarev
 */
@Component
@EnableScheduling
public class SchedulerServiceImpl implements SchedulingConfigurer, SchedulerService, DisposableBean, InitializingBean {
    private static final Log LOG = LogFactory.getLog(SchedulerServiceImpl.class);

    private static final long DAY_TIME = 24 * 60 * 60 * 1000;

    //Список запланированных задач планировщика
    private static final Map<String, ScheduledTask> tasks = new HashMap<String, ScheduledTask>();
    private static final Map<String, CronTask> cronTasks = new HashMap<String, CronTask>();
    private static final Map<String, String> crons = new HashMap<String, String>();
    private final Map<String, SchedulerTaskExecutor> executors = ImmutableMap.<String, SchedulerTaskExecutor>builder()
            .put("CLEAR_TEMP_DIR", new SchedulerTaskExecutor() {
                @Override
                public void execute() {
                    clearTempDirectory();
                }
            })
            .put("CLEAR_BLOB_DATA", new SchedulerTaskExecutor() {
                @Override
                public void execute() {
                    clearBlobData();
                }
            })
            .put("CLEAR_LOCK_DATA", new SchedulerTaskExecutor() {
                @Override
                public void execute() {
                    clearLockData();
                }
            })
            .put("LOG_TABLE_CHANGE_MONITORING", new SchedulerTaskExecutor() {
                @Override
                public void execute() {
                    taxEventsMonitoring();
                }
            })
            .put("ASYNC_TASK_MONITORING", new SchedulerTaskExecutor() {
                @Override
                public void execute() {
                    asyncTasksMonitoring();
                }
            })
            .put("SHRINK_TABLES", new SchedulerTaskExecutor() {
                @Override
                public void execute() {
                    shrinkTables();
                }
            })
            .build();

    //Реестр запланированных задач в Spring
    private ScheduledTaskRegistrar taskRegistrar;

    @Autowired
    private SchedulerTaskService schedulerTaskService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private AsyncTaskThreadContainer asyncTaskThreadContainer;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private TaxEventProcessor taxEventProcessor;
    @Autowired
    private ApplicationInfo applicationInfo;
    @Autowired
    private DBToolService dbToolService;

    /**
     * Инициализация планировщика
     */
    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("scheduler-thread-%d").build();
        return Executors.newScheduledThreadPool(10, namedThreadFactory);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        this.taskRegistrar = scheduledTaskRegistrar;
        taskRegistrar.setScheduler(taskExecutor());

        IntervalTask intervalTask = new IntervalTask(new Thread("SchedulerUpdateTask") {
            @Override
            public void run() {
                updateAllTask();
            }
        }, 60000);
        tasks.put("updateAllTask", taskRegistrar.scheduleFixedDelayTask(intervalTask));

        Runtime.getRuntime().addShutdownHook(new Thread("SchedulerShutdownHook") {
            @Override
            public void run() {
                for (ScheduledTask scheduledTask : tasks.values()) {
                    scheduledTask.cancel();
                }
            }
        });
    }

    /**
     * При старте приложения очищает назначение текущему узлу всех асинхронных задач. Чтобы задачи не оставались висеть, в случае если сервер некорректно завершил работу
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //TODO: (dloshkarev) возможно в этом случае будет лучше удалять задачи и их блокировки (как в dev-моде), т.к если стенд упал из-за какой то задачи, то она снова его положит
        asyncManager.releaseNodeTasks();
    }

    /**
     * При завершении работы приложения останавливает выполнение всех задач планировщика
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        shutdownAllTasks();
        asyncManager.releaseNodeTasks();
    }

    @Override
    public void shutdownAllTasks() {
        for (String settingCode : tasks.keySet()) {
            LOG.info("Shutdown task with code: " + settingCode);
            tasks.get(settingCode).cancel();
            crons.remove(settingCode);
            cronTasks.remove(settingCode);
        }
    }

    @Override
    public void updateAllTask() {
        for (final Map.Entry<String, SchedulerTaskExecutor> executor : executors.entrySet()) {
            //Планируем задачи с расписанием из БД на момент старта приложения
            String settingCode = executor.getKey();
            SchedulerTaskData schedulerTask = schedulerTaskService.fetchOne(SchedulerTask.valueOf(settingCode));
            if (schedulerTask == null) {
                LOG.error("Cannot find schedule for task with setting code = " + settingCode + ". Check database table 'CONFIGURATION_SCHEDULER'");
            } else if (schedulerTask.isActive()) {
                try {
                    scheduleTask(executor.getValue(), settingCode, schedulerTask.getSchedule());
                } catch (Exception e) {
                    LOG.error("Cannot set schedule for task with setting code = " + settingCode + ", cron format is incorrect! Check database table 'CONFIGURATION_SCHEDULER'");
                }
            }
        }
    }

    /**
     * Добавляет задачу в планировщик
     *
     * @param executor    метод, который планировщик должен вызвать
     * @param settingCode код строки с настройками задачи в БД
     * @param cron        расписание задачи
     */
    public void scheduleTask(final SchedulerTaskExecutor executor, final String settingCode, final String cron) {
        if (crons.containsKey(settingCode)) {
            String oldCron = crons.get(settingCode);
            if (oldCron == null && cron == null || oldCron != null && oldCron.equals(cron)) {
                //Если расписание не поменялось, то ничего не делаем
                return;
            }
        }
        //Удаляем существующую задачу
        if (tasks.containsKey(settingCode)) {
            LOG.info("Cancel scheduled task with code: " + settingCode);
            tasks.get(settingCode).cancel();
            crons.remove(settingCode);
            cronTasks.remove(settingCode);
        }
        if (cron != null) {
            //Добавляем задачу в список, для того, чтобы потом ее можно было при необходимости удалить из планировщика
            CronTask cronTask = new CronTask(
                    new Thread("SchedulerTask-" + settingCode) {
                        @Override
                        public void run() {
                            try {
                                MDC.put("processId", String.format("SchedulerId=%s code=%s ", Long.toString(System.currentTimeMillis(), 16), settingCode));
                                executor.execute();
                            } catch (Exception e) {
                                LOG.error(String.format("Cannot call executor for task with code: \"%s\"", settingCode), e);
                            } finally {
                                MDC.clear();
                            }
                        }
                    }, new CronTrigger(cron, TimeZone.getDefault())
            );
            tasks.put(settingCode, taskRegistrar.scheduleCronTask(cronTask));
            cronTasks.put(settingCode, cronTask);
            LOG.info(String.format("Scheduled task. settingCode: %s, cron: %s", settingCode, cron));
        }
        crons.put(settingCode, cron);
    }

    @Override
    public Date nextExecutionTime(String settingCode) {
        if (cronTasks.containsKey(settingCode)) {
            return cronTasks.get(settingCode).getTrigger().nextExecutionTime(new SimpleTriggerContext());
        }
        return null;
    }

    /**
     * Удаление загруженных архивов из папки с временными файлами
     */
    private void clearTempDirectory() {
        SchedulerTaskData schedulerTask = schedulerTaskService.fetchOne(SchedulerTask.CLEAR_TEMP_DIR);
        if (schedulerTask.isActive()) {
            LOG.info("Temp directory cleaning started by scheduler");
            schedulerTaskService.updateStartDate(SchedulerTask.CLEAR_TEMP_DIR);
            File tempPath = new File(System.getProperty("java.io.tmpdir"));
            File[] fileList = tempPath.listFiles();
            long currentDate = new Date().getTime();
            for (File tempFile : fileList) {
                if (tempFile.exists() && !tempFile.isHidden() && (currentDate - tempFile.lastModified()) > 30 * DAY_TIME) {
                    if (tempFile.isFile()) {
                        tempFile.delete();
                    } else {
                        try {
                            FileUtils.deleteDirectory(tempFile);
                        } catch (IOException e) {
                            // пропускаем
                        }
                    }
                }
            }
            LOG.info("Temp directory cleaning finished");
        }
    }

    /**
     * Задача для очистки BLOB_DATA, DECLARATION_REPORT, LOG по расписанию
     */
    @Transactional
    private void clearBlobData() {
        SchedulerTaskData schedulerTask = schedulerTaskService.fetchOne(SchedulerTask.CLEAR_BLOB_DATA);
        if (schedulerTask.isActive()) {
            LOG.info("BLOB_DATA cleaning started by scheduler");
            schedulerTaskService.updateStartDate(SchedulerTask.CLEAR_BLOB_DATA);
            blobDataService.clean();
            LOG.info("BLOB_DATA cleaning finished");
        }
    }

    /**
     * Задача для удаления блокировок, которые старше заданого времени
     */
    private void clearLockData() {
        SchedulerTaskData schedulerTask = schedulerTaskService.fetchOne(SchedulerTask.CLEAR_LOCK_DATA);
        if (schedulerTask.isActive()) {
            LOG.info("LOCK_DATA cleaning started by scheduler");
            schedulerTaskService.updateStartDate(SchedulerTask.CLEAR_LOCK_DATA);
            String secCountParam = schedulerTask.getParams().get(0).getValue();
            Long seconds = Long.parseLong(secCountParam);
            lockDataService.unlockIfOlderThan(seconds);
            LOG.info("LOCK_DATA cleaning finished");
        }
    }

    /**
     * Задача для мониторинга появление новых событий в УН, которые требуют обработки на стороне НДФЛ
     */
    private void taxEventsMonitoring() {
        if (applicationInfo.isProductionMode()) {
            SchedulerTaskData schedulerTask = schedulerTaskService.fetchOne(SchedulerTask.LOG_TABLE_CHANGE_MONITORING);
            if (schedulerTask.isActive()) {
                schedulerTaskService.updateStartDate(SchedulerTask.LOG_TABLE_CHANGE_MONITORING);
                taxEventProcessor.processTaxEvents();
            }
        }
    }

    /**
     * Задача для мониторинга появление новых асинхронных задач и их запуска
     */
    private void asyncTasksMonitoring() {
        asyncTaskThreadContainer.processQueues();
    }

    private void shrinkTables() {
        SchedulerTaskData schedulerTask = schedulerTaskService.fetchOne(SchedulerTask.SHRINK_TABLES);
        if (schedulerTask.isActive()) {
            LOG.info("shrinking tables started by scheduler");
            schedulerTaskService.updateStartDate(SchedulerTask.SHRINK_TABLES);
            dbToolService.shrinkTables();
            LOG.info("shrinking tables finished");
        }
    }

    /**
     * Обработчик задачи планировщика
     */
    private interface SchedulerTaskExecutor {
        /**
         * Метод который будет вызван по расписанию указанному для задачи планировщика
         */
        void execute();
    }
}
