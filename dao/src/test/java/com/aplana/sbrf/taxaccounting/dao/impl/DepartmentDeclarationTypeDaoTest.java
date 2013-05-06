package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
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
		List<DepartmentDeclarationType> ddt = departmentDeclarationTypeDao.getDestinations(2, 1, FormDataKind.PRIMARY);
		assertEquals(1, ddt.size());
		ddt = departmentDeclarationTypeDao.getDestinations(1, 1, FormDataKind.PRIMARY);
		assertEquals(2, ddt.size());
		ddt = departmentDeclarationTypeDao.getDestinations(1, 2, FormDataKind.CONSOLIDATED);
		assertEquals(3, ddt.size());
	}

	@Test
	public void testGetDepartmentIdsByTaxType() {
		Set<Integer> departmentIds = departmentDeclarationTypeDao.getDepartmentIdsByTaxType(TaxType.fromCode('T'));
		assertEquals(2, departmentIds.size());
		assertTrue(departmentIds.contains(1));
		assertTrue(departmentIds.contains(2));
		departmentIds = departmentDeclarationTypeDao.getDepartmentIdsByTaxType(TaxType.fromCode('I'));
		assertEquals(1, departmentIds.size());
		assertTrue(departmentIds.contains(1));
	}

	@Test
	public void testSave() {
		List<DepartmentDeclarationType> links = departmentDeclarationTypeDao.getDepartmentDeclarationTypes(1);
		// changing
		DepartmentDeclarationType link = links.get(0);
		link.setDeclarationTypeId(3);
		//adding
		DepartmentDeclarationType newLink = new DepartmentDeclarationType();
		newLink.setDeclarationTypeId(4);
		newLink.setDepartmentId(1);

		links.add(newLink);

		departmentDeclarationTypeDao.save(1, links);

		link = departmentDeclarationTypeDao.getDepartmentDeclarationTypes(1).get(1);
		newLink = departmentDeclarationTypeDao.getDepartmentDeclarationTypes(1).get(0);

		assertEquals(3, link.getDeclarationTypeId());
		assertEquals(1, link.getDepartmentId());

		assertEquals(4, newLink.getDeclarationTypeId());
		assertEquals(1, newLink.getDepartmentId());
	}

}
