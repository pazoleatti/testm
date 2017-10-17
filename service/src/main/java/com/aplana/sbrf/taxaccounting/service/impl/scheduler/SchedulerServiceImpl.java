package com.aplana.sbrf.taxaccounting.service.impl.scheduler;

import com.aplana.sbrf.taxaccounting.async.AsyncTaskThreadContainer;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.annotation.AnnotationUtil;
import com.aplana.sbrf.taxaccounting.model.annotation.AplanaScheduled;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.api.SchedulerTaskService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
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
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Класс для запуска периодических задач по расписанию
 * Является бином, чтобы не надо было создавать его экземпляр каждый раз при вызове методов и обеспечить его существование в единственном экземпляре
 * Обрабатываются методы ТОЛЬКО ЭТОГО класса помеченные {@link AplanaScheduled}, причем методы должны быть без параметров.
 * Планируется, что нужные сервисы будут вызываться уже из этого класса, это необходимо, т.к при получении методов через рефлексию нельзя получить экземпляр этого класса в спринговом конфиге,
 * а класс Scheduler уже является бином и соответственно ему доступны все остальные бины.
 *
 * @author dloshkarev
 */
@Component
@EnableScheduling
public class SchedulerServiceImpl implements SchedulingConfigurer, SchedulerService, DisposableBean {
    private static final Log LOG = LogFactory.getLog(SchedulerServiceImpl.class);

    private static final long DAY_TIME = 24 * 60 * 60 * 1000;

    //Список запланированных задач планировщика
    private static final Map<String, ScheduledTask> tasks = new HashMap<String, ScheduledTask>();
    private static final Map<String, CronTask> cronTasks = new HashMap<String, CronTask>();
    private static final Map<String, String> crons = new HashMap<String, String>();

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
    private ApplicationInfo applicationInfo;

    /**
     * Инициализация планировщика
     */
    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(100);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        this.taskRegistrar = scheduledTaskRegistrar;
        taskRegistrar.setScheduler(taskExecutor());

        IntervalTask intervalTask = new IntervalTask(new Runnable() {
            @Override
            public void run() {
                updateAllTask();
            }
        }, 60000);
        tasks.put("updateAllTask", taskRegistrar.scheduleFixedDelayTask(intervalTask));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (ScheduledTask scheduledTask : tasks.values()) {
                    scheduledTask.cancel();
                }
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        shutdownAllTasks();
    }

    @Override
    public void shutdownAllTasks() {
        for (String settingCode : tasks.keySet()) {
            LOG.debug("Shutdown task with code: " + settingCode);
            tasks.get(settingCode).cancel();
            crons.remove(settingCode);
            cronTasks.remove(settingCode);
        }
    }

    /**
     * Добавляет выполнение методов помеченных {@link AplanaScheduled} в планировщик
     */
    @Override
    public void updateAllTask() {
        Set<Method> methods = AnnotationUtil.findAllAnnotatedMethods(AplanaScheduled.class);
        for (final Method method : methods) {
            //Планируем задачи с расписанием из БД на момент старта приложения
            String settingCode = method.getAnnotation(AplanaScheduled.class).settingCode();
            SchedulerTaskData schedulerTask = schedulerTaskService.getSchedulerTask(SchedulerTask.valueOf(settingCode));
            if (schedulerTask == null) {
                LOG.error("Cannot find schedule for task with setting code = " + settingCode + ". Check database table 'CONFIGURATION_SCHEDULER'");
            } else if (schedulerTask.isActive()) {
                try {
                    scheduleTask(method, schedulerTask.getSchedule());
                } catch (Exception e) {
                    LOG.error("Cannot set schedule for task with setting code = " + settingCode + ", cron format is incorrect! Check database table 'CONFIGURATION_SCHEDULER'");
                }
            }
        }
    }

    /**
     * Добавляет задачу в планировщик
     *
     * @param method метод, который планировщик должен вызвать
     * @param cron   расписание задачи
     */
    public void scheduleTask(final Method method, final String cron) {
        final SchedulerServiceImpl scheduler = this;
        String settingCode = method.getAnnotation(AplanaScheduled.class).settingCode();
        if (crons.containsKey(settingCode)) {
            String oldCron = crons.get(settingCode);
            if (oldCron == null && cron == null || oldCron != null && oldCron.equals(cron)) {
                //Если расписание не поменялось, то ничего не делаем
                return;
            }
        }
        //Удаляем существующую задачу
        if (tasks.containsKey(settingCode)) {
            LOG.debug("Cancel scheduled method with code: " + settingCode);
            tasks.get(settingCode).cancel();
            crons.remove(settingCode);
            cronTasks.remove(settingCode);
        }
        LOG.debug("Scheduled method: " + method + ", with cron: " + cron);
        if (cron != null) {
            //Добавляем задачу в список, для того, чтобы потом ее можно было при необходимости удалить из планировщика
            CronTask cronTask = new CronTask(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                method.invoke(scheduler);
                            } catch (Exception e) {
                                throw new ServiceException(String.format("Cannot call method: \"%s\"", method.getName()), e);
                            }
                        }
                    }, new CronTrigger(cron, TimeZone.getDefault())
            );
            tasks.put(settingCode, taskRegistrar.scheduleCronTask(cronTask));
            cronTasks.put(settingCode, cronTask);
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
    @AplanaScheduled(settingCode = "CLEAR_TEMP_DIR")
    public void clearTempDirectory() {
        SchedulerTaskData schedulerTask = schedulerTaskService.getSchedulerTask(SchedulerTask.CLEAR_TEMP_DIR);
        if (schedulerTask.isActive()) {
            LOG.info("Temp directory cleaning started by scheduler");
            schedulerTaskService.updateTaskStartDate(SchedulerTask.CLEAR_TEMP_DIR);
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
    @AplanaScheduled(settingCode = "CLEAR_BLOB_DATA")
    @Transactional
    public void clearBlobData() {
        SchedulerTaskData schedulerTask = schedulerTaskService.getSchedulerTask(SchedulerTask.CLEAR_BLOB_DATA);
        if (schedulerTask.isActive()) {
            LOG.info("BLOB_DATA cleaning started by scheduler");
            schedulerTaskService.updateTaskStartDate(SchedulerTask.CLEAR_BLOB_DATA);
            blobDataService.clean();
            LOG.info("BLOB_DATA cleaning finished");
        }
    }

    /**
     * Задача для удаления блокировок, которые старше заданого времени
     */
    @AplanaScheduled(settingCode = "CLEAR_LOCK_DATA")
    public void clearLockData() {
        SchedulerTaskData schedulerTask = schedulerTaskService.getSchedulerTask(SchedulerTask.CLEAR_LOCK_DATA);
        if (schedulerTask.isActive()) {
            LOG.info("LOCK_DATA cleaning started by scheduler");
            schedulerTaskService.updateTaskStartDate(SchedulerTask.CLEAR_LOCK_DATA);
            String secCountParam = schedulerTask.getParams().get(0).getValue();
            Long seconds = Long.parseLong(secCountParam);
            lockDataService.unlockIfOlderThan(seconds);
            LOG.info("LOCK_DATA cleaning finished");
        }
    }

    /**
     * Задача для мониторинга появление новых асинхронных задач и их запуска
     */
    @AplanaScheduled(settingCode = "ASYNC_TASK_MONITORING")
    public void asyncTasksMonitoring() {
        SchedulerTaskData schedulerTask = schedulerTaskService.getSchedulerTask(SchedulerTask.ASYNC_TASK_MONITORING);
        if (schedulerTask.isActive()) {
            schedulerTaskService.updateTaskStartDate(SchedulerTask.ASYNC_TASK_MONITORING);
            asyncTaskThreadContainer.processQueues();
        }
    }
}
