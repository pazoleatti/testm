package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.CalendarDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
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
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 16.11.2016 14:38
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"CalendarDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CalendarDaoTest{

	@Autowired
	private CalendarDao dao;

	private Date getDate(int y, int m, int d) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(y, m, d);
		return cal.getTime();
	}

	private static Date truncate(Date date) {
		return DateUtils.truncate(date, Calendar.DATE);
	}

	@Test
	public void testGetWorkDay() {
		assertEquals(truncate(getDate(2016, 1, 1)), truncate(dao.getWorkDay(getDate(2016, 0, 1), 15)));
		assertEquals(truncate(getDate(2016, 1, 2)), truncate(dao.getWorkDay(getDate(2015, 11, 30), 16)));
		assertEquals(truncate(getDate(2016, 0, 18)), truncate(dao.getWorkDay(getDate(2016, 0, 12), 4)));
		assertEquals(truncate(getDate(2017, 0, 15)), truncate(dao.getWorkDay(getDate(2016, 11, 30), 16)));
		assertEquals(truncate(getDate(2017, 0, 1)), truncate(dao.getWorkDay(getDate(2016, 11, 30), 2)));
		assertEquals(truncate(getDate(2020, 4, 17)), truncate(dao.getWorkDay(getDate(2016, 11, 30), 1234)));
		assertEquals(truncate(getDate(2016, 0, 15)), truncate(dao.getWorkDay(getDate(2016, 0, 22), -5)));
		assertEquals(truncate(getDate(2016, 0, 18)), truncate(dao.getWorkDay(getDate(2016, 0, 22), -4)));
		assertEquals(truncate(getDate(2016, 0, 7)), truncate(dao.getWorkDay(getDate(2016, 0, 11), -4)));
	}

	@Test(expected = DaoException.class)
	public void testGetWorkDay2() {
		dao.getWorkDay(null, 3);
	}

	@Test
	public void testGetWorkDayCount() {
		assertEquals(5, dao.getWorkDayCount(getDate(2016, 0, 6), getDate(2016, 0, 16)));
		assertEquals(16, dao.getWorkDayCount(getDate(2016, 11, 30), getDate(2017, 0, 14)));
		assertEquals(16, dao.getWorkDayCount(getDate(2015, 11, 30), getDate(2016, 0, 28)));
		assertEquals(4, dao.getWorkDayCount(getDate(2016, 0, 12), getDate(2016, 0, 15)));
		assertEquals(16, dao.getWorkDayCount(getDate(2016, 11, 30), getDate(2017, 0, 14)));
		assertEquals(2, dao.getWorkDayCount(getDate(2016, 11, 30), getDate(2016, 11, 31)));
		assertEquals(1096, dao.getWorkDayCount(getDate(2017, 0, 1), getDate(2020, 0, 1)));
	}

	@Test
	public void testGetDateDif() {
		assertEquals(2, dao.getDateDif(getDate(2015, 11, 30), getDate(2016, 0, 1)));
		assertEquals(4, dao.getDateDif(getDate(2016, 0, 3), getDate(2016, 0, 7)));
	}
}