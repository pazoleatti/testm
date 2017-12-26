package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.SchedulerTaskDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskModel;
import com.aplana.sbrf.taxaccounting.service.SchedulerTaskService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SchedulerTaskServiceImpl implements SchedulerTaskService {

    @Autowired
    private SchedulerTaskDao schedulerTaskDao;
    @Autowired
    private SchedulerService schedulerService;

    @Override
    public SchedulerTaskData getSchedulerTask(SchedulerTask task) {
        return schedulerTaskDao.fetchOneSchedulerTask(task.getSchedulerTaskId());
    }

    @Override
    public SchedulerTaskData getSchedulerTask(Long taskId) {
        return schedulerTaskDao.fetchOneSchedulerTask(taskId );
    }
    @Override
    public List<SchedulerTaskData> getAllSchedulerTask() {
        return schedulerTaskDao.fetchAllSchedulerTasks();
    }

    @Override
    public PagingResult<TaskSearchResultItem> fetchAllSchedulerTasks(PagingParams pagingParams) {
        List<TaskSearchResultItem> records = new ArrayList<TaskSearchResultItem>();

        PagingResult<SchedulerTaskModel> tasks = schedulerTaskDao.fetchAllSchedulerTasks(pagingParams);
        for (SchedulerTaskModel task : tasks) {
            TaskSearchResultItem item = new TaskSearchResultItem();
            item.setId((long) task.getId());
            item.setName(task.getTaskName());
            item.setSchedule(task.getSchedule());
            item.setState(task.getSchedule() != null ? (task.getActive() == 1 ? "Активна" : "Остановлена") : "Не задано расписание");
            item.setModificationDate(task.getModificationDate().toString("dd-MM-yyyy, HH:mm:ss"));
            item.setLastFireTime(task.getLastFireDate() != null ? task.getLastFireDate().toString("dd-MM-yyyy, HH:mm:ss") : "");
            Date nextFireTime = schedulerService.nextExecutionTime(SchedulerTask.getByTaskId(task.getId()).name());
            item.setNextFireTime(nextFireTime != null ? new SimpleDateFormat("dd-MM-yyyy, HH:mm:ss").format(nextFireTime) : "");
            records.add(item);
        }

        return new PagingResult<TaskSearchResultItem>(records, tasks.getTotalCount());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void setActiveSchedulerTask(boolean active, List<Long> ids) {
        schedulerTaskDao.setActiveSchedulerTask(active, ids);
    }

    @Override
    public void updateTaskStartDate(SchedulerTask task) {
        schedulerTaskDao.updateTaskStartDate(task.getSchedulerTaskId());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String updateTask(SchedulerTaskData taskData) {
        String result = null;
        if (!validateSchedule(taskData.getSchedule())) {
            result = " Значение атрибута «Расписание» не соответствует требованиям формата Cron!";
        } else {
            schedulerTaskDao.updateTask(taskData);
        }
        return result;
    }

    @Override
    public boolean validateSchedule(String schedule) {
        try {
            new CronSequenceGenerator(schedule);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
