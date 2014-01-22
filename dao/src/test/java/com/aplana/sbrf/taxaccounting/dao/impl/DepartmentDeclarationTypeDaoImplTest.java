package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DepartmentDeclarationTypeDaoImplTest.xml" })
@Transactional
public class DepartmentDeclarationTypeDaoImplTest {
	
	@Autowired
	DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
	
	@Test
	public void saveSucsess(){
		departmentDeclarationTypeDao.save(5, 4);
	}
	
	@Test(expected = DaoException.class) 
	public void saveError(){
		departmentDeclarationTypeDao.save(5, 4);
		departmentDeclarationTypeDao.save(5, 4);
	}

	@Test
	public void testGetDepartmentDeclarationTypes() {
		List<DepartmentDeclarationType> ddt = departmentDeclarationTypeDao.getDepartmentDeclarationTypes(1);
		assertEquals(2, ddt.size());
	}

	@Test
	public void testGetDestanations() {
		List<DepartmentDeclarationType> ddt = departmentDeclarationTypeDao.getDestinations(2, 1, FormDataKind.PRIMARY);
		assertEquals(1, ddt.size());
		ddt = departmentDeclarationTypeDao.getDestinations(1, 1, FormDataKind.PRIMARY);
		assertEquals(2, ddt.size());
		ddt = departmentDeclarationTypeDao.getDestinations(1, 2, FormDataKind.CONSOLIDATED);
		assertEquals(2, ddt.size());
	}

	@Test
	public void testGetDepartmentIdsByTaxType() {
		Set<Integer> departmentIds = departmentDeclarationTypeDao.getDepartmentIdsByTaxType(TaxType.fromCode('T'));
		assertEquals(2, departmentIds.size());
		assertTrue(departmentIds.contains(1));
		assertTrue(departmentIds.contains(2));
		departmentIds = departmentDeclarationTypeDao.getDepartmentIdsByTaxType(TaxType.fromCode('I'));
		assertEquals(3, departmentIds.size());
		assertTrue(departmentIds.contains(1));
        assertTrue(departmentIds.contains(7));
        assertTrue(departmentIds.contains(9));
	}
}
