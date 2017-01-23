package com.aplana.sbrf.taxaccounting.model.refbook;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 06.02.14 18:24
 */

public class RefBookTest {

	@Test
	public void test() {
		// поленился написать тест. есть логика в модельном классе, ее нужно протестить. один раз уже нашел ошибку,
		// в связи с этим и возникло желание написать юнит-тесты :(
	}

	@Test
	public void idEnumGetByIdReturnsEnum() throws Exception {
		RefBook.Id actualId = RefBook.Id.getById(923L);
		Assert.assertEquals(RefBook.Id.REGION, actualId);
	}

	@Test
	public void idEnumGetByIdReturnsNullOnWrongId() throws Exception {
		RefBook.Id id = RefBook.Id.getById(-1983L);
		Assert.assertNull(id);
	}
}
