package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;

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
		when(dao.get("a",false)).thenReturn(new LockData("a", 0));
		when(dao.get("b",false)).thenReturn(new LockData("b", 0));
		when(dao.get("c",false)).thenReturn(new LockData("c", 1));
		ReflectionTestUtils.setField(service, "dao", dao);

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
		Assert.assertNull(service.lock("b", 1, "", "")); // можно заблокировать - просроченная блокировка
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

}