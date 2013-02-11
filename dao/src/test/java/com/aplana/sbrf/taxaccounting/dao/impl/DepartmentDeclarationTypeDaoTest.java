package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DepartmentDeclarationTypeDaoTest.xml" })
@Transactional
public class DepartmentDeclarationTypeDaoTest {
	@Autowired
	DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

	@Test
	public void testGetDepartmentDeclarationTypes() {
		List<DepartmentDeclarationType> ddt = departmentDeclarationTypeDao.getDepartmentDeclarationTypes(1);
		assertEquals(2, ddt.size());
	}

	@Test
	public void testGetDestanations() {
		List<DepartmentDeclarationType> ddt = departmentDeclarationTypeDao.getDestanations(2, 1, FormDataKind.PRIMARY);
		assertEquals(1, ddt.size());
		ddt = departmentDeclarationTypeDao.getDestanations(1, 1, FormDataKind.PRIMARY);
		assertEquals(2, ddt.size());
		ddt = departmentDeclarationTypeDao.getDestanations(1, 2, FormDataKind.CONSOLIDATED);
		assertEquals(3, ddt.size());
	}

}
