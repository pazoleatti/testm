package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 15:19
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"LockDataDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LockDataDaoTest extends Assert {

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

		Assert.assertNull(dao.get("c", false));
	}

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
		Assert.assertNull(dao.get("a", false));
	}

	@Test(expected = LockException.class)
	public void deleteLockTest2() {
		dao.deleteLock("qwerty");
	}

	@Test
	public void getUsersWaitingForLock() {
		List<Integer> uids = dao.getUsersWaitingForLock("a");
		assertEquals(2, uids.size());
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

	@Test(expected = LockException.class)
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

}
