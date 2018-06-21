package com.aplana.sbrf.taxaccounting.mock;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DeclarationTypeMockUtils {

    private DeclarationTypeMockUtils() {}

	public static DeclarationType mockDeclarationType(int id, TaxType taxType) {
		DeclarationType dt = mock(DeclarationType.class);
		when(dt.getId()).thenReturn(id);
		return dt;
	}

	public static DeclarationType mockDeclarationType(int id) {
		DeclarationType dt = mock(DeclarationType.class);
		when(dt.getId()).thenReturn(id);
		return dt;
	}
}
