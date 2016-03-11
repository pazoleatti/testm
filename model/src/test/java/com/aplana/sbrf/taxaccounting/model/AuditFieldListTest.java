package com.aplana.sbrf.taxaccounting.model;

import static com.aplana.sbrf.taxaccounting.model.AuditFieldList.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 09.03.2016 16:14
 */

public class AuditFieldListTest {

	@Test
	public void getSortedValues() {
		AuditFieldList[] values = AuditFieldList.getSortedValues();
		assertEquals(ALL, values[0]);
		assertEquals(EVENT, values[1]);
		assertEquals(NOTE, values[2]);
		assertEquals(PERIOD, values[3]);
		assertEquals(DEPARTMENT, values[4]);
		assertEquals(TYPE, values[5]);
		assertEquals(FORM_KIND, values[6]);
		assertEquals(FORM_DECLARATION_TYPE, values[7]);
		assertEquals(USER, values[8]);
		assertEquals(ROLE, values[9]);
		assertEquals(USER_DEPARTMENT, values[10]);
		assertEquals(IP, values[11]);
		assertEquals(SERVER, values[12]);
	}
}