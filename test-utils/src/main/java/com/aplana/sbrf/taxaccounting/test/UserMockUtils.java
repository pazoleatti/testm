package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.TAUser;

public class UserMockUtils {
	public static TAUser mockUser(int id, int departmentId, String roleCode) {
		TAUser user = mock(TAUser.class);
		when(user.getId()).thenReturn(id);
		when(user.getDepartmentId()).thenReturn(departmentId);
		when(user.hasRole(roleCode)).thenReturn(true);
		return user;
	}
}
