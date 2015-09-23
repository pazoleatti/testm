package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import com.aplana.sbrf.taxaccounting.model.LockData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 15:19
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"LockDataDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LockDataDaoTest {

	@Autowired
	LockDataDao dao;

	@Test
	public void getTest() {
		LockData data = dao.get("a", false);
		Assert.assertEquals(0, data.getUserId());
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2013, 0, 1, 0, 5, 0);
        data = dao.get("a", cal.getTime());
        Assert.assertNotNull(data);
        cal.set(Calendar.YEAR, 2014);
        data = dao.get("a", cal.getTime());
        Assert.assertNull(data);

        data = dao.get("FORM_DATA", true);
        Assert.assertNotNull(data);
        data = dao.get("FORM_DATA_2", true);
        Assert.assertNull(data);
	}

	@Test
	public void getTest2() {
		Assert.assertNull(dao.get("c", false));
	}

	/*@Test// (expected = LockException.class)
	public void createLockTest() {
		dao.createLock("c", 0, 1, "", "", "");
	}*/

	@Test (expected = LockException.class)
	public void createLockTest() {
		dao.createLock("a", 0, "", "", ""); // дубликат
	}

	@Test
	public void createLockTest2() {
		dao.createLock("c", 0, "", "", "");
		LockData data = dao.get("c", false);
		Assert.assertEquals("c", data.getKey());
		Assert.assertEquals(0, data.getUserId());

        dao.createLock("abc", 0, "", "", "test");
        data = dao.get("abc", false);
        data = dao.get("abc", data.getDateLock());
        Assert.assertNotNull(data);
        Assert.assertEquals(data.getServerNode(), "test");
	}

	@Test
	public void deleteLockTest() {
		dao.deleteLock("a");
	}

	@Test
	public void deleteLockTest2() {
		dao.deleteLock("a");
		Assert.assertNull(dao.get("a", false));
	}
}
