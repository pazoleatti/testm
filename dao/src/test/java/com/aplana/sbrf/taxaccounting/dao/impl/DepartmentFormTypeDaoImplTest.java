package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DepartmentFormTypeDaoImplTest.xml" })
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DepartmentFormTypeDaoImplTest {

	@Autowired
	DepartmentFormTypeDao departmentFormTypeDao;

	@Test
	public void saveSucsess(){
		departmentFormTypeDao.save(1, 5, 2);
	}

	@Test(expected = DaoException.class)
	public void saveError(){
		departmentFormTypeDao.save(1, 1, 2);
		departmentFormTypeDao.save(1, 1, 2);
	}

	@Test
	public void getByDep(){
		assertEquals(7, departmentFormTypeDao.getByDepartment(1).size());
	}

	@Test
	public void getByDepAndTaxType(){
		assertEquals(3, departmentFormTypeDao.getByTaxType(1, TaxType.TRANSPORT, null, null).size());
	}

	@Test
	public void getFormSources(){
		//assertEquals(5, departmentFormTypeDao.getFormSources(2, 1, FormDataKind.fromId(3), null, null).size());
        assertEquals(6, departmentFormTypeDao.getFormSources(2, 0, null, null, null).size());
	}

	@Test
	public void getDeclarationSources(){
		assertEquals(4, departmentFormTypeDao.getDeclarationSources(2, 1, null, null).size());
        assertEquals(4, departmentFormTypeDao.getDeclarationSources(2, 0, null, null).size());
	}

	@Test
     public void getFormDestinations(){
        assertEquals(2, departmentFormTypeDao.getFormDestinations(1, 1, FormDataKind.fromId(3), null, null).size());
        assertEquals(3, departmentFormTypeDao.getFormDestinations(1, 0, null, null, null).size());
    }

    @Test
    public void getFormDestinationsForDestDep(){
        //Hsqldb не поддерживает иерархические запросы
        /*assertEquals(0, departmentFormTypeDao.getFormDestinationsWithDepId(1, 1, Arrays.asList(TaxType.TRANSPORT)).size());
        assertEquals(0, departmentFormTypeDao.getFormDestinationsWithDepId(1, 1, null).size());*/
    }

	@Test
	public void getDeclarationDestinations1(){
		assertEquals(1, departmentFormTypeDao.getDeclarationDestinations(3, 1, FormDataKind.fromId(3), null, null).size());
        assertEquals(1, departmentFormTypeDao.getDeclarationDestinations(3, 0,null, null, null).size());
	}

	@Test
     public void getDeclarationDestinations2(){
        assertEquals(0, departmentFormTypeDao.getDeclarationDestinations(1, 1, FormDataKind.fromId(3), null, null).size());
    }

    @Test
    public void getDeclarationDestinationsForDestDep(){
        //Hsqldb не поддерживает иерархические запросы
        //assertEquals(0, departmentFormTypeDao.getDeclarationDestinationsWithDepId(1, 1, Arrays.asList(TaxType.TRANSPORT)).size());
    }

	@Test
	public void testGetAllSources() {
		assertEquals(5, departmentFormTypeDao.getDepartmentSources(2, TaxType.TRANSPORT, null, null).size());
        assertEquals(9, departmentFormTypeDao.getDepartmentSources(2, null, null, null).size());
	}

    @Test
    public void testGetFormTypeBySource() {
        ArrayList<FormDataKind> formDataKinds = new ArrayList<FormDataKind>(3);
        formDataKinds.add(FormDataKind.ADDITIONAL);
        formDataKinds.add(FormDataKind.CONSOLIDATED);
        formDataKinds.add(FormDataKind.PRIMARY);
        /*Assert.assertEquals(1, departmentFormTypeDao.getFormTypeBySource(1, TaxType.INCOME, formDataKinds).size());*/
    }

    /**
     * Существование форм назначений
     */
    public void existAssignedForm(){
        assertTrue("В подразделении 2 есть форма с типом 1 и видом 3", departmentFormTypeDao.existAssignedForm(2, 1, FormDataKind.SUMMARY));
        assertFalse("В подразделении 2 есть форма с типом 1 и видом 3", departmentFormTypeDao.existAssignedForm(2, 1, FormDataKind.CONSOLIDATED));
    }

    @Test
    public void findDestinationsForFormTypeTest(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 1);
        Date dateStart = calendar.getTime();
        calendar.set(2014, Calendar.DECEMBER, 31);
        Date dateEnd = calendar.getTime();
        assertEquals(2, departmentFormTypeDao.findDestinationsForFormType(1, dateStart, dateEnd).size());
    }

    @Test
    public void findSourcesForFormTypeTest(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 1);
        Date dateStart = calendar.getTime();
        calendar.set(2014, Calendar.DECEMBER, 31);
        Date dateEnd = calendar.getTime();
        List<Pair<DepartmentFormType, Date>> pairs = departmentFormTypeDao.findSourcesForFormType(2, dateStart, dateEnd);
        assertEquals(1, pairs.size());
    }

    @Test
    public void getDFTByFTTest(){
        assertEquals(3, departmentFormTypeDao.getDFTByFormType(1).size());
    }
}
