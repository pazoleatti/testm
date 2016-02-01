package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import com.aplana.sbrf.taxaccounting.model.LockData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
		dao.lock("a", 0, "", "", ""); // дубликат
	}

	@Test
	public void createLockTest2() {
		dao.lock("c", 0, "", "", "");
		LockData data = dao.get("c", false);
		Assert.assertEquals("c", data.getKey());
		Assert.assertEquals(0, data.getUserId());

        dao.lock("abc", 0, "", "", "test");
        data = dao.get("abc", false);
        data = dao.get("abc", data.getDateLock());
        Assert.assertNotNull(data);
        Assert.assertEquals(data.getServerNode(), "test");
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
	public void getUsersWaitingForLock() {
		List<Integer> uids = dao.getUsersWaitingForLock("a");
		assertEquals(2, uids.size());
		uids = dao.getUsersWaitingForLock("b");
		assertEquals(0, uids.size());

		dropTable();
		uids = dao.getUsersWaitingForLock("b");
		assertEquals(0, uids.size());
	}

	@Test
	 public void addUserWaitingForLock() {
		dao.addUserWaitingForLock("b", 2);
		dao.addUserWaitingForLock("b", 0);
		dao.addUserWaitingForLock("b", 1);
		List<Integer> uids = dao.getUsersWaitingForLock("b");
		assertEquals(3, uids.size());
	}

	@Test
	public void addUserWaitingForLock2() {
		dao.addUserWaitingForLock("a", 1);
	}

	@Test
	public void getLocks() {
		PagingParams paging = new PagingParams(0, 100);
		PagingResult<LockData> data = dao.getLocks("", LockData.LockQueues.ALL, paging);
		assertEquals(5, data.size());
		data = dao.getLocks("a", LockData.LockQueues.ALL, paging);
		assertEquals(4, data.size());
		data = dao.getLocks("", LockData.LockQueues.SHORT, paging);
		assertEquals(0, data.size());
		data = dao.getLocks("", LockData.LockQueues.LONG, paging);
		assertEquals(0, data.size());
		data = dao.getLocks("", LockData.LockQueues.NONE, paging);
		assertEquals(5, data.size());
		data = dao.getLocks(null, LockData.LockQueues.NONE, paging);
		assertEquals(5, data.size());
		data = dao.getLocks("a", null, paging);
		assertEquals(4, data.size());
		data = dao.getLocks("non exists", LockData.LockQueues.ALL, paging);
		assertEquals(0, data.size());
	}

	@Test
	public void unlockAll() {
		List<String> keys = Arrays.asList(new String[] {"a", "q"});
		dao.unlockAll(keys);
		// проверяем, что блокировки были удалены
		PagingParams paging = new PagingParams(0, 100);
		PagingResult<LockData> data = dao.getLocks("", LockData.LockQueues.ALL, paging);
		assertEquals(3, data.size());
	}

	@Test
	public void updateState() {
		LockData lock = dao.get("a", false);
		dao.updateState(lock.getKey(), lock.getDateLock(), ":)", ":(");
		lock = dao.get("a", false);
		assertEquals(":)", lock.getState());
		assertEquals(":(", lock.getServerNode());
	}

	@Test
	public void updateQueue() {
		LockData lock = dao.get("a", false);
		dao.updateQueue(lock.getKey(), lock.getDateLock(), LockData.LockQueues.ALL);
		lock = dao.get("a", false);
		assertEquals(LockData.LockQueues.ALL, lock.getQueue());
		dao.updateQueue(lock.getKey(), lock.getDateLock(), LockData.LockQueues.SHORT);
		lock = dao.get("a", false);
		assertEquals(LockData.LockQueues.SHORT, lock.getQueue());
		dao.updateQueue(lock.getKey(), lock.getDateLock(), LockData.LockQueues.LONG);
		lock = dao.get("a", false);
		assertEquals(LockData.LockQueues.LONG, lock.getQueue());
		dao.updateQueue(lock.getKey(), lock.getDateLock(), LockData.LockQueues.NONE);
		lock = dao.get("a", false);
		assertEquals(LockData.LockQueues.NONE, lock.getQueue());
	}

	@Test
	public void unlockIfOlderThan() throws InterruptedException {
		assertEquals(1, dao.unlockIfOlderThan(1));
		Thread.sleep(2000);
		assertEquals(4, dao.unlockIfOlderThan(1));
		// создаем новую блокировку
		dao.lock("test_key", 1, "test_description", "test_state", "test server");
		Thread.sleep(1000);
		assertEquals(0, dao.unlockIfOlderThan(2));
		Thread.sleep(2000);
		assertEquals(1, dao.unlockIfOlderThan(2));
	}

	/**
	 * Метод предназначен для проверки обработки исключительных ситуаций
	 */
	private void dropTable() {
		jdbc.update("ALTER TABLE lock_data_subscribers DROP CONSTRAINT lock_data_subscr_fk_lock_data", new HashMap());
		jdbc.update("DROP TABLE lock_data", new HashMap());
	}

	@Test
	public void checkExceptions() {
		dropTable();
		try {
			dao.unlockIfOlderThan(0);
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
		PagingResult<LockData> locks = dao.getLocks("", LockData.LockQueues.ALL, new PagingParams(0, 10));
		System.out.println(locks);
		assertEquals(3, locks.size());

		dropTable();
		dao.unlockAllByUserId(0, true);
	}

}
