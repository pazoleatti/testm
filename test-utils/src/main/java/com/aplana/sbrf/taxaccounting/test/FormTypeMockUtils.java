package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

public final class FormTypeMockUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private FormTypeMockUtils() {
	}

	public static FormType mockFormType(int id, TaxType taxType, String name) {
		FormType ft = mock(FormType.class);
		when(ft.getId()).thenReturn(id);
		when(ft.getName()).thenReturn(name);
		when(ft.getTaxType()).thenReturn(taxType);
		return ft;
	}
}
