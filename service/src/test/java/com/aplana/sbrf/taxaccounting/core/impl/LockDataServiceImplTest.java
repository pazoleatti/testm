package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

	@BeforeClass
	public static void init() {
		service = new LockDataServiceImpl();
		LockDataDao dao = mock(LockDataDao.class);
		when(dao.get("a",false)).thenReturn(new LockData("a", 0, FUTURE));
		when(dao.get("b",false)).thenReturn(new LockData("b", 0, PAST));
		when(dao.get("c",false)).thenReturn(new LockData("c", 1, FUTURE));
		ReflectionTestUtils.setField(service, "dao", dao);

        TransactionHelper tx = new TransactionHelper() {
            @Override
            public void executeInNewTransaction(TransactionLogic logic) {
                logic.execute();
            }

            @Override
            public <T> T returnInNewTransaction(TransactionLogic<T> logic) {
                return logic.executeWithReturn();
            }
        };
        ReflectionTestUtils.setField(service, "tx", tx);
	}

	@Test
	public void lockTest() {
		Assert.assertNotNull(service.lock("a", 0, "", "", 0)); // есть активная блокировка
		Assert.assertNotNull(service.lock("a", 1, "", "", 0)); // есть активная блокировка
		Assert.assertNull(service.lock("z", 0, "", "", 0)); // можно заблокировать
		Assert.assertNull(service.lock("b", 1, "", "", 0)); // можно заблокировать - просроченная блокировка
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

	@Test
	public void extendTest() {
		service.extend("a", 0, 0);
		service.extend("b", 0, 0);
		service.extend("b", 1, 0);
		service.extend("c", 1, 0);
	}

	@Test (expected = ServiceException.class)
	public void extendTest2() {
		service.extend("a", 1, 0);
	}

}