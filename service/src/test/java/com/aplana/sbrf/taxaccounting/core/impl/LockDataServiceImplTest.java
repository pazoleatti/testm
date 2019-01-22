package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.impl.TAUserDaoImpl;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.impl.LockDataServiceImpl;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.Mockito.*;

/**
 * Тестирование сервиса блокировок
 */
public class LockDataServiceImplTest {

    private static LockDataService service;
    private static TAUserDao userDao;
    private static LockDataDao dao;
    private static AuditService auditService;

    @Before
    public void init() {
        service = new LockDataServiceImpl();
        userDao = mock(TAUserDaoImpl.class);
        auditService = mock(AuditService.class);
        dao = mock(LockDataDao.class);
        when(userDao.getUser(0)).thenReturn(new TAUser());
        when(dao.findByKey("a")).thenReturn(new LockData("a", 0));
        when(dao.findByKey("b")).thenReturn(new LockData("b", 0));
        when(dao.findByKey("c")).thenReturn(new LockData("c", 1));
        ReflectionTestUtils.setField(service, "dao", dao);
        ReflectionTestUtils.setField(service, "userDao", userDao);
        ReflectionTestUtils.setField(service, "auditService", auditService);

        TransactionHelper tx = new TransactionHelper() {
            @Override
            public <T> T executeInNewTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }

            @Override
            public <T> T executeInNewReadOnlyTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }
        };
        ReflectionTestUtils.setField(service, "tx", tx);
    }

	@Test
	public void lockTest() {
		Assert.assertNotNull(service.lock("a", 0, "")); // есть активная блокировка
		Assert.assertNotNull(service.lock("a", 1, "")); // есть активная блокировка
		Assert.assertNull(service.lock("z", 0, "")); // можно заблокировать
	}

    @Test(expected = ServiceException.class)
    public void unlockTest2() {
        service.unlock("a", 1); // попытка разблокировать другим пользователем
    }

    @Test
    public void unlockTest4() {
        service.unlock("a", 0);
        service.unlock("c", 1);
    }

    private static LockData getLockData(String key) {
        LockData lock = new LockData();
        lock.setKey(key);
        lock.setDateLock(new Date());
        lock.setDescription("descr");
        return lock;
    }

    @Test
    public void unlockIfOlderThan() {
        when(dao.getLockIfOlderThan(10)).thenReturn(Arrays.asList("lock1", "lock2"));
        when(dao.findByKey("lock1")).thenReturn(getLockData("lock1"));
        when(dao.findByKey("lock2")).thenReturn(getLockData("lock2"));

        service.unlockIfOlderThan(10);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(dao, times(2)).unlock(argument.capture());
        verify(auditService, times(2)).add(eq(FormDataEvent.DELETE_LOCK), any(TAUserInfo.class), isNull(Integer.class), isNull(Integer.class), isNull(String.class),
                isNull(String.class), isNull(AuditFormType.class), isNull(Integer.class), any(String.class), isNull(String.class));

        Assert.assertEquals("lock1", argument.getAllValues().get(0));
        Assert.assertEquals("lock2", argument.getAllValues().get(1));
    }
}