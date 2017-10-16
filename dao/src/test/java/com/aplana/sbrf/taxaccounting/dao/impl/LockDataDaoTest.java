package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 15:19
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"LockDataDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LockDataDaoTest extends Assert {

	@Autowired
	private LockDataDao dao;
	@Autowired
	private NamedParameterJdbcTemplate jdbc;

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

		Assert.assertNull(dao.get("c", false));
	}

	@Test (expected = LockException.class)
	public void createLockTest() {
		dao.lock("a", 0, ""); // дубликат
	}

	@Test
	public void createLockTest2() {
		dao.lock("c", 0, "");
		LockData data = dao.get("c", false);
		Assert.assertEquals("c", data.getKey());
		Assert.assertEquals(0, data.getUserId());

        dao.lock("abc", 0, "");
        data = dao.get("abc", false);
        data = dao.get("abc", data.getDateLock());
        Assert.assertNotNull(data);
	}

	@Test
	public void deleteLockTest() {
		dao.unlock("a");
		Assert.assertNull(dao.get("a", false));
	}

	@Test(expected = LockException.class)
	public void deleteLockTest2() {
		dao.unlock("qwerty");
	}

	@Test
	public void getLocks() {
		PagingParams paging = new PagingParams(0, 100);
		PagingResult<LockData> data = dao.getLocks("", paging);
		assertEquals(5, data.size());
		data = dao.getLocks("a", paging);
		assertEquals(4, data.size());
		data = dao.getLocks("", paging);
		assertEquals(5, data.size());
		data = dao.getLocks("", paging);
		assertEquals(5, data.size());
		data = dao.getLocks(null, paging);
		assertEquals(5, data.size());
		data = dao.getLocks("a", paging);
		assertEquals(4, data.size());
		data = dao.getLocks("non exists", paging);
		assertEquals(0, data.size());
	}

	@Test
	public void unlockAll() {
		List<Long> ids = Arrays.asList(1L, 5L);
		dao.unlockAll(ids);
		// проверяем, что блокировки были удалены
		PagingParams paging = new PagingParams(0, 100);
		PagingResult<LockData> data = dao.getLocks("", paging);
		assertEquals(3, data.size());
	}

	private int unlockIfOlderThan(long seconds) {
		List<String> keyList = dao.getLockIfOlderThan(seconds);
		for (String key : keyList) {
			dao.unlock(key);
		}
		return keyList.size();
	}

	@Test
	public void unlockIfOlderThan() throws InterruptedException {
		assertEquals(1, unlockIfOlderThan(1));
		Thread.sleep(2000);
		assertEquals(4, unlockIfOlderThan(1));
		// создаем новую блокировку
		dao.lock("test_key", 1, "test_description");
		Thread.sleep(1000);
		assertEquals(0, unlockIfOlderThan(2));
		Thread.sleep(2000);
		assertEquals(1, unlockIfOlderThan(2));
	}

	/**
	 * Метод предназначен для проверки обработки исключительных ситуаций
	 */
	private void dropTable() {
		jdbc.update("DROP TABLE lock_data", new HashMap());
	}

	@Test
	public void checkExceptions() {
		dropTable();
		try {
			unlockIfOlderThan(0);
		} catch (LockException e) {
			assertTrue(e.getMessage().startsWith("Ошибка при удалении"));
		}
		try {
			dao.get("asd", true);
		} catch (LockException e) {
			assertTrue(e.getMessage().startsWith("Ошибка при поиске блокировки"));
		}
		try {
			dao.get("asd", new Date());
		} catch (LockException e) {
			assertTrue(e.getMessage().startsWith("Ошибка при поиске блокировки"));
		}
		try {
			dao.unlock("asd");
		} catch (LockException e) {
			assertTrue(e.getMessage().startsWith("Ошибка при удалении"));
		}
		try {
			dao.unlockAllByUserId(0, false);
		} catch (LockException e) {
			assertTrue(e.getMessage().startsWith("Ошибка при удалении блокировок для пользователя"));
		}
	}

	@Test
	public void get() {
		LockData lock = dao.get("FORM_DATA_1", true);
		assertEquals("FORM_DATA_1", lock.getKey());
		assertEquals(2, lock.getUserId());

		lock = dao.get("q", false);
		assertEquals("q", lock.getKey());
		assertEquals(0, lock.getUserId());

		lock = dao.get("a", false);
		assertEquals("a", lock.getKey());
		Date dateLock = lock.getDateLock();

		lock = dao.get("awdfzf zf", true);
		assertNull(lock);

		lock = dao.get("a", dateLock);
		assertEquals("a", lock.getKey());
		assertEquals(dateLock, lock.getDateLock());

		lock = dao.get("awdfzf zf", dateLock);
		assertNull(lock);
	}

	@Test
	public void unlockAllByUserId() {
		dao.unlockAllByUserId(0, false);
		PagingResult<LockData> locks = dao.getLocks("", new PagingParams(0, 10));
		System.out.println(locks);
		assertEquals(2, locks.size());

		dropTable();
		dao.unlockAllByUserId(0, true);
	}

}
