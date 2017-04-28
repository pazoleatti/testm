package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SchedulerTaskDao;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParamType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

	@Test
	public void get1() {
		SchedulerTaskData task = dao.get(SchedulerTask.CLEAR_BLOB_DATA.getSchedulerTaskId());
		assertEquals(SchedulerTask.CLEAR_BLOB_DATA, task.getTask());
		assertEquals("Очистка файлового хранилища", task.getTaskName());
		assertEquals("0 15 22 * * ?", task.getSchedule());
		assertEquals(true, task.isActive());
		assertEquals(0, task.getParams().size());
	}

	@Test
	public void get2() {
		SchedulerTaskData task = dao.get(SchedulerTask.CLEAR_LOCK_DATA.getSchedulerTaskId());
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

	@Test
	public void getAll() {
		List<SchedulerTaskData> tasks = dao.getAll();
		assertEquals(3, tasks.size());

		assertEquals(SchedulerTask.CLEAR_BLOB_DATA, tasks.get(0).getTask());
		assertEquals("Очистка файлового хранилища", tasks.get(0).getTaskName());
		assertEquals("0 15 22 * * ?", tasks.get(0).getSchedule());
		assertEquals(true, tasks.get(0).isActive());
		assertEquals(0, tasks.get(0).getParams().size());
		assertEquals(new GregorianCalendar(2013, Calendar.MARCH, 31).getTime(), tasks.get(0).getModificationDate());
		assertEquals(new GregorianCalendar(2015, Calendar.MARCH, 31).getTime(), tasks.get(0).getLast_fire_date());


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

	@Test
	public void updateTask() {
		SchedulerTaskData task = dao.get(SchedulerTask.CLEAR_BLOB_DATA.getSchedulerTaskId());
		task.setSchedule("15 15 22 * * ?");
		dao.updateTask(task);
		task = dao.get(SchedulerTask.CLEAR_BLOB_DATA.getSchedulerTaskId());
		assertEquals("15 15 22 * * ?", task.getSchedule());
	}

	@Test
	public void setActiveSchedulerTask() {
		SchedulerTaskData task = dao.get(SchedulerTask.CLEAR_BLOB_DATA.getSchedulerTaskId());
		assertEquals(true, task.isActive());
		dao.setActiveSchedulerTask(false, Arrays.asList(SchedulerTask.CLEAR_BLOB_DATA.getSchedulerTaskId()));
		task = dao.get(SchedulerTask.CLEAR_BLOB_DATA.getSchedulerTaskId());
		assertEquals(false, task.isActive());
		dao.setActiveSchedulerTask(true, Arrays.asList(SchedulerTask.CLEAR_BLOB_DATA.getSchedulerTaskId()));
		task = dao.get(SchedulerTask.CLEAR_BLOB_DATA.getSchedulerTaskId());
		assertEquals(true, task.isActive());
	}
}