package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.impl.TAUserDaoImpl;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.hamcrest.core.AnyOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.Any;
import org.mockito.internal.matchers.AnyVararg;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.Mockito.*;

/**
 * Тестирование сервиса блокировок
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 18.07.14 12:13
 */

public class LockDataServiceImplTest {

	private static final Date NOW = new Date();
	private static final Date FUTURE = new Date(NOW.getTime() + 100000);
	private static final Date PAST = new Date(NOW.getTime() - 100000);

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
		when(dao.get("a",false)).thenReturn(new LockData("a", 0));
		when(dao.get("b",false)).thenReturn(new LockData("b", 0));
		when(dao.get("c",false)).thenReturn(new LockData("c", 1));
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
        ServerInfo serverInfo = mock(ServerInfo.class);
        ReflectionTestUtils.setField(service, "serverInfo", serverInfo);
	}

	@Test
	public void lockTest() {
		Assert.assertNotNull(service.lock("a", 0, "", "")); // есть активная блокировка
		Assert.assertNotNull(service.lock("a", 1, "", "")); // есть активная блокировка
		Assert.assertNull(service.lock("z", 0, "", "")); // можно заблокировать
	}

	@Test (expected = ServiceException.class)
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
		when(dao.get("lock1", false)).thenReturn(getLockData("lock1"));
		when(dao.get("lock2", false)).thenReturn(getLockData("lock2"));

		service.unlockIfOlderThan(10);

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(dao, times(2)).unlock(argument.capture());
		verify(auditService, times(2)).add(eq(FormDataEvent.DELETE_LOCK), any(TAUserInfo.class), isNull(Integer.class), isNull(Integer.class), isNull(String.class),
				isNull(String.class), isNull(Integer.class), any(String.class), isNull(String.class));

		Assert.assertEquals("lock1", argument.getAllValues().get(0));
		Assert.assertEquals("lock2", argument.getAllValues().get(1));
	}
}