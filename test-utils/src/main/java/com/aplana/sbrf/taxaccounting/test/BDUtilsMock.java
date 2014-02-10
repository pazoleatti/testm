package com.aplana.sbrf.taxaccounting.test;

import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Обертка для класса {@link BDUtils}
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 05.02.14 14:33
 */

public class BDUtilsMock {

	// Иммитация Sequence
	private static long cnt = 100;

	public static BDUtils getBDUtils() {
		BDUtils bdUtils = mock(BDUtils.class);
		when(bdUtils.getNextIds(any(BDUtils.Sequence.class), anyLong())).thenAnswer(new Answer<List<Long>>() {
			@Override
			public List<Long> answer(InvocationOnMock invocationOnMock) throws Throwable {
				List<Long> ids = new ArrayList<Long>();
				Object[] args = invocationOnMock.getArguments();
				int count = ((Long) args[1]).intValue();
				for (int i = 0; i < count; i++) {
					ids.add(cnt++);
				}
				return ids;
			}
		});
		when(bdUtils.getNextDataRowIds(anyLong())).thenAnswer(new Answer<List<Long>>() {
			@Override
			public List<Long> answer(InvocationOnMock invocationOnMock) throws Throwable {
				List<Long> ids = new ArrayList<Long>();
				Object[] args = invocationOnMock.getArguments();
				int count = ((Long) args[0]).intValue();
				for (int i = 0; i < count; i++) {
					ids.add(cnt++);
				}
				return ids;
			}
		});
		when(bdUtils.getNextRefBookRecordIds(anyLong())).thenAnswer(new org.mockito.stubbing.Answer<List<Long>>() {
			@Override
			public List<Long> answer(InvocationOnMock invocationOnMock) throws Throwable {
				List<Long> ids = new ArrayList<Long>();
				Object[] args = invocationOnMock.getArguments();
				int count = ((Long) args[0]).intValue();
				for (int i = 0; i < count; i++) {
					ids.add(cnt++);
				}
				return ids;
			}
		});
		return bdUtils;
	}

	/**
	 * Возвращает значение текущего счетчика
	 * @return
	 */
	public static long getIteratorValue() {
		return cnt;
	}
}
