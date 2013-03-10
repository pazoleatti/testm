package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;

/**
 * Утилиты для создания mock-объектов о привязке налоговых форм к подразделениям 
 * @author dsultanbekov
 */
public final class DepartmentFormTypeMockUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private DepartmentFormTypeMockUtils() {
	}

	public static DepartmentFormType mockDepartmentFormType(int departmentId, int formTypeId, FormDataKind formDataKind) {
		DepartmentFormType res = mock(DepartmentFormType.class);
		when(res.getDepartmentId()).thenReturn(departmentId);
		when(res.getFormTypeId()).thenReturn(formTypeId);
		when(res.getKind()).thenReturn(formDataKind);
		return res;
	}
}
