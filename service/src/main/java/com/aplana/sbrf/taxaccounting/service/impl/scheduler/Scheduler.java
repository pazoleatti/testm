package com.aplana.sbrf.taxaccounting.service.impl.scheduler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.annotation.AnnotationUtil;
import com.aplana.sbrf.taxaccounting.model.annotation.AplanaScheduled;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

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
public class Scheduler implements SchedulingConfigurer {
    private static final Log LOG = LogFactory.getLog(Scheduler.class);

    private static final long DAY_TIME = 24 * 60 * 60 * 1000;

    //Список запланированных задач планировщика
    private static final Map<String, ScheduledTask> tasks = new HashMap<String, ScheduledTask>();
    private static final Map<String, String> crons = new HashMap<String, String>();

    //Реестр запланированных задач в Spring
    private ScheduledTaskRegistrar taskRegistrar;

    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private DepartmentService departmentService;

    /**
     * Инициализация планировщика
     */
    @Bean(destroyMethod="shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(100);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        this.taskRegistrar = scheduledTaskRegistrar;
        taskRegistrar.setScheduler(taskExecutor());
    }

    /**
     * Добавляет выполнение методов помеченных {@link AplanaScheduled} в планировщик
     */
    @Scheduled(fixedDelay = 60000)
    public void updateAllTask() {
        Set<Method> methods = AnnotationUtil.findAllAnnotatedMethods(AplanaScheduled.class);
        for (final Method method : methods) {
            //Планируем задачи с расписанием из БД на момент старта приложения
            String settingCode = method.getAnnotation(AplanaScheduled.class).settingCode();
            String schedule = null;
            try {
                int rootDepartmentId = departmentService.getBankDepartment().getId();
                List<String> schedules = configurationService.get(settingCode).get(ConfigurationParam.CLEAR_TEMP_DIR_CRON, rootDepartmentId);
                if (schedules != null && !schedules.isEmpty()) {
                    schedule = schedules.get(0);
                }
            } catch (Exception e) {
                LOG.error("Cannot find schedule for task with setting code = " + settingCode + ". Check database table 'CONFIGURATION'");
            }
            try {
                scheduleTask(method, schedule);
            } catch (Exception e) {
                LOG.error("Cannot set schedule for task with setting code = " + settingCode + ", cron format is incorrect! Check database table 'CONFIGURATION'");
            }
        }
    }

    /**
     * Добавляет задачу в планировщик
     *
     * @param method        метод, который планировщик должен вызвать
     * @param cron          расписание задачи
     */
    public void scheduleTask(final Method method, final String cron) {
        final Scheduler scheduler = this;
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
        }
        LOG.debug("Scheduled method: " + method + ", with cron: " + cron);
        if (cron != null) {
            //Добавляем задачу в список, для того, чтобы потом ее можно было при необходимости удалить из планировщика
            tasks.put(
                    settingCode,
                    taskRegistrar.scheduleCronTask(new CronTask(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        method.invoke(scheduler);
                                    } catch (Exception e) {
                                        throw new ServiceException(String.format("Cannot call method: \"%s\"", method.getName()), e);
                                    }
                                }
                            }, cron
                    ))
            );
        }
        crons.put(settingCode, cron);
    }

    /**
     * Удаление загруженных архивов из папки с временными файлами
     */
    @AplanaScheduled(settingCode = "CLEAR_TEMP_DIR_CRON")
    public void clearTempDirectory() {
        LOG.info("Temp directory cleaning started by scheduler");
        File tempPath = new File(System.getProperty("java.io.tmpdir"));
        File[] fileList = tempPath.listFiles();
        long currentDate = new Date().getTime();
        for(File tempFile: fileList) {
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
