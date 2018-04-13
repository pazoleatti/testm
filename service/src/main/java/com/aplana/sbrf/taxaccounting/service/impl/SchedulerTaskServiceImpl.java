package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.SchedulerTaskDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.service.SchedulerTaskService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private FastDateFormat dateFormatter = FastDateFormat.getInstance("dd-MM-yyyy, HH:mm:ss");

    @Override
    public SchedulerTaskData fetchOne(SchedulerTask task) {
        return schedulerTaskDao.fetchOne(task.getSchedulerTaskId());
    }

    @Override
    public SchedulerTaskData fetchOne(Long taskId) {
        return schedulerTaskDao.fetchOne(taskId);
    }

    @Override
    public List<SchedulerTaskData> fetchAll() {
        return schedulerTaskDao.fetchAll();
    }

    @Override
    public PagingResult<TaskSearchResultItem> fetchAllByPaging(PagingParams pagingParams) {
        List<TaskSearchResultItem> records = new ArrayList<>();

        PagingResult<SchedulerTaskData> tasks = schedulerTaskDao.fetchAllByPaging(pagingParams);
        for (SchedulerTaskData task : tasks) {
            TaskSearchResultItem item = new TaskSearchResultItem();
            item.setId(task.getTask().getSchedulerTaskId());
            item.setName(task.getTaskName());
            item.setSchedule(task.getSchedule());
            item.setState(task.getSchedule() != null ? (task.isActive() ? "Активна" : "Остановлена") : "Не задано расписание");
            item.setModificationDate(dateFormatter.format(task.getModificationDate()));
            item.setLastFireTime(task.getLastFireDate() != null ? dateFormatter.format(task.getLastFireDate()) : "");
            Date nextFireTime = schedulerService.nextExecutionTime(task.getTask().name());
            item.setNextFireTime(nextFireTime != null ? dateFormatter.format(nextFireTime) : "");
            records.add(item);
        }

        return new PagingResult<>(records, tasks.getTotalCount());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void updateActiveByIds(boolean active, List<Long> ids) {
        schedulerTaskDao.updateActiveByIds(active, ids);
    }

    @Override
    public void updateStartDate(SchedulerTask task) {
        schedulerTaskDao.updateStartDate(task.getSchedulerTaskId());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void update(SchedulerTaskData taskData) {
        schedulerTaskDao.update(taskData);
    }

    @Override
    public boolean validateScheduleCronString(String schedule) {
        try {
            new CronSequenceGenerator(schedule);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
