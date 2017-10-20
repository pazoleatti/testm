package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DepartmentDeclarationTypeDaoImplTest.xml" })
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
    public void testGetByTaxType() {
        assertEquals(2, departmentDeclarationTypeDao.getByTaxType(1, null, null, null).size());
    }

	@Test
    @Ignore("Только НДФЛ используется в проекте")
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

    @Test
    public void getDDTByFTTest(){
        assertEquals(2, departmentDeclarationTypeDao.getDDTByDeclarationType(1).size());
        assertEquals(0, departmentDeclarationTypeDao.getDDTByDeclarationType(1000).size());
    }
}
