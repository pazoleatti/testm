package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;

public class DeclarationTypeMockUtils {
	public static DeclarationType mockDeclarationType(int id) {
		DeclarationType dt = mock(DeclarationType.class);
		when(dt.getId()).thenReturn(id);
		return dt;
	}
}
