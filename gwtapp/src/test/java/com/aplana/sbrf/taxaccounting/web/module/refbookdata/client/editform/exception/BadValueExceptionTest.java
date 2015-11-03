package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception;

import org.junit.Test;

import java.util.Collections;

import static junit.framework.Assert.assertEquals;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 12.11.14 16:23
 */

public class BadValueExceptionTest {

	@Test
	public void test() {
		BadValueException bve = new BadValueException(Collections.singletonMap("Василий Пупкин", "Александрович"));
		assertEquals("Атрибут \"Василий Пупкин\": Александрович", bve.iterator().next());
	}
}
