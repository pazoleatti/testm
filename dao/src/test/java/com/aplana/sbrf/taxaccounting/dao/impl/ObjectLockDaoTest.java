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

import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.LockException;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.ObjectLock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ObjectLockDaoTest.xml"})
public class ObjectLockDaoTest {
	@Autowired
	private ObjectLockDao objectLockDao; 

	@Test
	public void testGetObjectLockNotLocked() {
		ObjectLock<Long> lock = objectLockDao.getObjectLock(41l, FormData.class);
		assertNull(lock);
	}
	
	@Test
	public void testGetObjectLock() {
		ObjectLock<Long> lock = objectLockDao.getObjectLock(42l, FormData.class);
		assertEquals(42l, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());
	}
	
	@Test
	public void testGetObjectLockTimedOut() {
		ObjectLock<Long> lock = objectLockDao.getObjectLock(43l, FormData.class);
		assertNull(lock);
	}
	
	
	@Test
	public void testLock() {
		objectLockDao.lockObject(1l, FormData.class, 1);
		ObjectLock<Long> lock = objectLockDao.getObjectLock(1l, FormData.class);
		assertEquals(1l, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());
	}
	
	@Test
	public void testLockAlreadyLockedBySameUser() {
		objectLockDao.lockObject(2l, FormData.class, 1);
		ObjectLock<Long> lock = objectLockDao.getObjectLock(2l, FormData.class);
		assertEquals(2l, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());
	}
	
	@Test
	public void testLockAlreadyLockedBySameUserButTimedOut() {
		objectLockDao.lockObject(3l, FormData.class, 1);
		ObjectLock<Long> lock = objectLockDao.getObjectLock(3l, FormData.class);
		assertEquals(3l, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());		
	}
	
	@Test(expected=LockException.class)
	public void testLockAlreadyLockedByDifferentUser() {
		objectLockDao.lockObject(4l, FormData.class, 1);
	}
	
	@Test
	public void testLockAlreadyLockedByDifferentUserButTimedOut() {
		objectLockDao.lockObject(5l, FormData.class, 1);
		ObjectLock<Long> lock = objectLockDao.getObjectLock(5l, FormData.class);
		assertEquals(5l, lock.getObjectId());
		assertEquals(FormData.class, lock.getObjectClass());
		assertEquals(1, lock.getUserId());		
	}

	@Test(expected=LockException.class)
	public void testUnlockNotLocked() {
		objectLockDao.unlockObject(11l, FormData.class, 1);
	}	
	
	@Test
	public void testUnlock() {
		objectLockDao.unlockObject(12l, FormData.class, 1);
		assertNull(objectLockDao.getObjectLock(12l, FormData.class));
	}
	
	@Test
	public void testUnlockLockedButTimedOut() {
		objectLockDao.unlockObject(13l, FormData.class, 1);
		assertNull(objectLockDao.getObjectLock(13l, FormData.class));
	}
	
	@Test(expected=LockException.class)
	public void testUnlockLockedByDifferentUser() {
		objectLockDao.unlockObject(14l, FormData.class, 1);
	}
	
	@Test(expected=LockException.class)
	public void testUnlockLockedByDifferentUserButTimedOut() {
		objectLockDao.unlockObject(15l, FormData.class, 1);
	}
	
	@Test
	public void testIsLocked() {
		assertFalse(objectLockDao.isLockedByUser(21l, FormData.class, 1));
		assertTrue(objectLockDao.isLockedByUser(22l, FormData.class, 1));
		assertFalse(objectLockDao.isLockedByUser(23l, FormData.class, 1));
		assertFalse(objectLockDao.isLockedByUser(24l, FormData.class, 1));
		assertFalse(objectLockDao.isLockedByUser(25l, FormData.class, 1));
	}
	
	@Test(expected=LockException.class)
	public void testRefreshLockNotLocked() {
		objectLockDao.refreshLock(31l, FormData.class, 1);
	}	
	
	@Test
	public void testRefreshLock() {
		objectLockDao.refreshLock(32l, FormData.class, 1);
		assertNotNull(objectLockDao.getObjectLock(32l, FormData.class));
	}
	
	@Test(expected=LockException.class)
	public void testRefreshLockLockedButTimedOut() {
		objectLockDao.refreshLock(33l, FormData.class, 1);
	}
	
	@Test(expected=LockException.class)
	public void testRefreshLockLockedByDifferentUser() {
		objectLockDao.refreshLock(34l, FormData.class, 1);
	}
	
	@Test(expected=LockException.class)
	public void testtestRefreshLockLockedByDifferentUserButTimedOut() {
		objectLockDao.refreshLock(35l, FormData.class, 1);
	}

}