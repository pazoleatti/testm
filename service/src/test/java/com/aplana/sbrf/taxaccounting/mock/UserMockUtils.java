package com.aplana.sbrf.taxaccounting.mock;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UserMockUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private UserMockUtils() {
	}

	public static TAUser mockUser(int id, int departmentId, final String roleCode) {
		TAUser user = mock(TAUser.class);
		when(user.getId()).thenReturn(id);
		when(user.getDepartmentId()).thenReturn(departmentId);
		when(user.hasRole(roleCode)).thenReturn(true);
        Answer<Boolean> answer = new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                for (int i = 0; i < invocation.getArguments().length; i++) {
                    Object argument = invocation.getArguments()[i];
                    if (argument instanceof String && argument.equals(roleCode)) {
                        return true;
                    }
                }
                return false;
            }
        };
        when(user.hasRole(any(TaxType.class), any(String.class))).then(answer);
        when(user.hasRoles(any(TaxType.class), any(String.class))).then(answer);
        when(user.hasRoles(any(TaxType.class), any(String.class), any(String.class))).then(answer);
        when(user.hasRoles(any(TaxType.class), any(String.class), any(String.class), any(String.class))).then(answer);
        when(user.hasRoles(any(TaxType.class), any(String.class), any(String.class), any(String.class), any(String.class))).then(answer);
        when(user.hasRoles(any(TaxType.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class))).then(answer);
        when(user.hasRoles(any(TaxType.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class))).then(answer);
        when(user.hasRoles(any(TaxType.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class))).then(answer);
        when(user.hasRoles(any(TaxType.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class))).then(answer);
        when(user.hasRoles(any(TaxType.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class))).then(answer);
        return user;
	}
}
