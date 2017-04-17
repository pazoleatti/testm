package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.CalendarDao;
import com.aplana.sbrf.taxaccounting.dao.SchedulerTaskDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

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
	public void get() {
		SchedulerTaskData task = dao.get(1L);
		assertEquals(SchedulerTask.CLEAR_BLOB_DATA, task.getTask());
		assertEquals("Очистка файлового хранилища", task.getTaskName());
		assertEquals("0 15 22 * * ?", task.getSchedule());
		assertEquals(true, task.isActive());
		assertEquals(0, task.getParams().size());
	}
}