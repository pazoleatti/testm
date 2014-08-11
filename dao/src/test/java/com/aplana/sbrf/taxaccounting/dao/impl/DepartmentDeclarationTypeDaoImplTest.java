package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    public void findDestinationsForFormTypeTest(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 1);
        Date dateStart = calendar.getTime();
        calendar.set(2014, Calendar.DECEMBER, 31);
        Date dateEnd = calendar.getTime();
        assertEquals(2, departmentDeclarationTypeDao.findDestinationDTsForFormType(1, dateStart, dateEnd).size());
    }

    @Test
    public void findSourcesForFormTypeTest(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 1);
        Date dateStart = calendar.getTime();
        calendar.set(2014, Calendar.DECEMBER, 31);
        Date dateEnd = calendar.getTime();
        List<Pair<DepartmentFormType, Date>> pairs = departmentDeclarationTypeDao.findSourceFTsForDeclaration(2, dateStart, dateEnd);
        assertEquals(1, pairs.size());
    }

    @Test
    public void getDDTByFTTest(){
        assertEquals(2, departmentDeclarationTypeDao.getDDTByDeclarationType(1).size());
        assertEquals(0, departmentDeclarationTypeDao.getDDTByDeclarationType(1000).size());
    }
}
