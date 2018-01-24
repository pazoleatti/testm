package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SchedulerTaskDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParamType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask.CLEAR_BLOB_DATA;
import static org.junit.Assert.assertEquals;

/**
 * @author lhaziev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"SchedulerTaskDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SchedulerTaskDaoTest {

    @Autowired
    private SchedulerTaskDao dao;

    /**
     * Получение задачи CLEAR_BLOB_DATA и проверка ее параметров
     */
    @Test
    public void getClearBlobData() {
        SchedulerTaskData task = dao.fetchOne(CLEAR_BLOB_DATA.getSchedulerTaskId());
        assertEquals(CLEAR_BLOB_DATA, task.getTask());
        assertEquals("Очистка файлового хранилища", task.getTaskName());
        assertEquals("0 15 22 * * ?", task.getSchedule());
        assertEquals(true, task.isActive());
        assertEquals(0, task.getParams().size());
    }

    /**
     * Получение задачи CLEAR_LOCK_DATA и проверка ее параметров
     */
    @Test
    public void getClearLockData() {
        SchedulerTaskData task = dao.fetchOne(SchedulerTask.CLEAR_LOCK_DATA.getSchedulerTaskId());
        assertEquals(SchedulerTask.CLEAR_LOCK_DATA, task.getTask());
        assertEquals("Удаление истекших блокировок", task.getTaskName());
        assertEquals("0 10 22 * * ?", task.getSchedule());
        assertEquals(false, task.isActive());
        assertEquals(1, task.getParams().size());
        assertEquals(20, task.getParams().get(0).getId());
        assertEquals("Время жизни блокировки (секунд)", task.getParams().get(0).getParamName());
        assertEquals(SchedulerTaskParamType.LONG, task.getParams().get(0).getParamType());
        assertEquals("172800", task.getParams().get(0).getValue());
    }

    /**
     * Получение всех задач и проверка параметров каждой из них
     */
    @Test
    public void getAllTasks() {
        List<SchedulerTaskData> tasks = dao.fetchAll();
        assertEquals(3, tasks.size());

        assertEquals(CLEAR_BLOB_DATA, tasks.get(0).getTask());
        assertEquals("Очистка файлового хранилища", tasks.get(0).getTaskName());
        assertEquals("0 15 22 * * ?", tasks.get(0).getSchedule());
        assertEquals(true, tasks.get(0).isActive());
        assertEquals(0, tasks.get(0).getParams().size());
        assertEquals(new LocalDateTime(2013, 3, 31, 0, 0).toDate(), tasks.get(0).getModificationDate());
        assertEquals(new LocalDateTime(2015, 3, 31, 0, 0).toDate(), tasks.get(0).getLastFireDate());


        assertEquals(SchedulerTask.CLEAR_LOCK_DATA, tasks.get(1).getTask());
        assertEquals("Удаление истекших блокировок", tasks.get(1).getTaskName());
        assertEquals("0 10 22 * * ?", tasks.get(1).getSchedule());
        assertEquals(false, tasks.get(1).isActive());
        assertEquals(0, tasks.get(1).getParams().size());


        assertEquals(SchedulerTask.CLEAR_TEMP_DIR, tasks.get(2).getTask());
        assertEquals("Очистка каталога временных файлов", tasks.get(2).getTaskName());
        assertEquals("0 5 22 * * ?", tasks.get(2).getSchedule());
        assertEquals(true, tasks.get(2).isActive());
        assertEquals(0, tasks.get(2).getParams().size());
    }

    /**
     * Получение страницы задач
     */
    @Test
    public void fetchAllByPaging() {
        List<SchedulerTaskData> tasks = dao.fetchAllByPaging(PagingParams.getInstance(2, 1));
        assertEquals(1, tasks.size());
        assertEquals(SchedulerTask.CLEAR_LOCK_DATA, tasks.get(0).getTask());
    }

    /**
     * Получение задачи CLEAR_BLOB_DATA и ее обновление
     */
    @Test
    public void updateClearBlobData() {
        SchedulerTaskData task = dao.fetchOne(CLEAR_BLOB_DATA.getSchedulerTaskId());
        task.setSchedule("15 15 22 * * ?");
        dao.update(task);
        task = dao.fetchOne(CLEAR_BLOB_DATA.getSchedulerTaskId());
        assertEquals("15 15 22 * * ?", task.getSchedule());
    }

    @Test
    public void updateStartDate() {
        dao.updateStartDate(CLEAR_BLOB_DATA.getSchedulerTaskId());
        SchedulerTaskData task = dao.fetchOne(CLEAR_BLOB_DATA.getSchedulerTaskId());
        assertEquals(LocalDate.now(), new LocalDate(task.getLastFireDate()));
    }

    /**
     * Изменение активности задачи CLEAR_BLOB_DATA
     */
    @Test
    public void changeActiveClearBlobData() {
        SchedulerTaskData task = dao.fetchOne(CLEAR_BLOB_DATA.getSchedulerTaskId());
        assertEquals(true, task.isActive());
        dao.updateActiveByIds(false, Arrays.asList(CLEAR_BLOB_DATA.getSchedulerTaskId()));
        task = dao.fetchOne(CLEAR_BLOB_DATA.getSchedulerTaskId());
        assertEquals(false, task.isActive());
        dao.updateActiveByIds(true, Arrays.asList(CLEAR_BLOB_DATA.getSchedulerTaskId()));
        task = dao.fetchOne(CLEAR_BLOB_DATA.getSchedulerTaskId());
        assertEquals(true, task.isActive());
    }
}