package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

public class DeclarationTypeMockUtils {
	public static DeclarationType mockDeclarationType(int id, TaxType taxType) {
		DeclarationType dt = mock(DeclarationType.class);
		when(dt.getId()).thenReturn(id);
		when(dt.getTaxType()).thenReturn(taxType);
		return dt;
	}

	public static DeclarationType mockDeclarationType(int id) {
		DeclarationType dt = mock(DeclarationType.class);
		when(dt.getId()).thenReturn(id);
		return dt;
	}
}
