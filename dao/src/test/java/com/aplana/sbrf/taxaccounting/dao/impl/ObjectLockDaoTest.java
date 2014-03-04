package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.LockException;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.ObjectLock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ObjectLockDaoTest.xml"})
@Transactional
public class ObjectLockDaoTest {
	@Autowired
	private ObjectLockDao objectLockDao; 

	@Test
	public void testGetObjectLockNotLocked() {
		ObjectLock<Long> lock = objectLockDao.getObjectLock(41L, FormData.class);
		assertNull(lock);
	}
	
	@Test
	public void testGetObjectLock() {
		ObjectLock<Long> lock = objectLockDao.getObjectLock(42L, FormData.class);
		assertEquals(42L, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());
	}
	
	@Test
	public void testGetObjectLockTimedOut() {
		ObjectLock<Long> lock = objectLockDao.getObjectLock(43L, FormData.class);
		assertNull(lock);
	}
	
	
	@Test
	public void testLock() {
		objectLockDao.lockObject(1L, FormData.class, 1);
		ObjectLock<Long> lock = objectLockDao.getObjectLock(1L, FormData.class);
		assertEquals(1L, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());
	}
	
	@Test
	public void testLockAlreadyLockedBySameUser() {
		objectLockDao.lockObject(2L, FormData.class, 1);
		ObjectLock<Long> lock = objectLockDao.getObjectLock(2L, FormData.class);
		assertEquals(2L, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());
	}
	
	@Test
	public void testLockAlreadyLockedBySameUserButTimedOut() {
		objectLockDao.lockObject(3L, FormData.class, 1);
		ObjectLock<Long> lock = objectLockDao.getObjectLock(3L, FormData.class);
		assertEquals(3L, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());		
	}
	
	@Test(expected=LockException.class)
	public void testLockAlreadyLockedByDifferentUser() {
		objectLockDao.lockObject(4L, FormData.class, 1);
	}
	
	@Test
	public void testLockAlreadyLockedByDifferentUserButTimedOut() {
		objectLockDao.lockObject(5L, FormData.class, 1);
		ObjectLock<Long> lock = objectLockDao.getObjectLock(5L, FormData.class);
		assertEquals(5L, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());		
	}

	@Test(expected=LockException.class)
	public void testUnlockNotLocked() {
		objectLockDao.unlockObject(11L, FormData.class, 1);
	}	
	
	@Test
	public void testUnlock() {
		objectLockDao.unlockObject(12L, FormData.class, 1);
		assertNull(objectLockDao.getObjectLock(12L, FormData.class));
	}
	
	@Test
	public void testUnlockLockedButTimedOut() {
		objectLockDao.unlockObject(13L, FormData.class, 1);
		assertNull(objectLockDao.getObjectLock(13L, FormData.class));
	}
	
	@Test(expected=LockException.class)
	public void testUnlockLockedByDifferentUser() {
		objectLockDao.unlockObject(14L, FormData.class, 1);
	}
	
	@Test(expected=LockException.class)
	public void testUnlockLockedByDifferentUserButTimedOut() {
		objectLockDao.unlockObject(15L, FormData.class, 1);
	}
	
	@Test
	public void testIsLocked() {
		assertFalse(objectLockDao.isLockedByUser(21L, FormData.class, 1));
		assertTrue(objectLockDao.isLockedByUser(22L, FormData.class, 1));
		assertFalse(objectLockDao.isLockedByUser(23L, FormData.class, 1));
		assertFalse(objectLockDao.isLockedByUser(24L, FormData.class, 1));
		assertFalse(objectLockDao.isLockedByUser(25L, FormData.class, 1));
	}
	
	@Test(expected=LockException.class)
	public void testRefreshLockNotLocked() {
		objectLockDao.refreshLock(31L, FormData.class, 1);
	}	
	
	@Test
	public void testRefreshLock() {
		objectLockDao.refreshLock(32L, FormData.class, 1);
		assertNotNull(objectLockDao.getObjectLock(32L, FormData.class));
	}
	
	@Test(expected=LockException.class)
	public void testRefreshLockLockedButTimedOut() {
		objectLockDao.refreshLock(33L, FormData.class, 1);
	}
	
	@Test(expected=LockException.class)
	public void testRefreshLockLockedByDifferentUser() {
		objectLockDao.refreshLock(34L, FormData.class, 1);
	}
	
	@Test(expected=LockException.class)
	public void testtestRefreshLockLockedByDifferentUserButTimedOut() {
		objectLockDao.refreshLock(35L, FormData.class, 1);
	}

}