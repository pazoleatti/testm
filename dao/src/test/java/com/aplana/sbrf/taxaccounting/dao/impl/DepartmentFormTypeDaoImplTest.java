package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.*;


@Ignore("Налоговые формы не используются!")
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
    public void getFormAssigned() {
        List<FormTypeKind> result = departmentFormTypeDao.getFormAssigned(1L, 'T');
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getId().intValue());
        assertEquals(5, result.get(1).getId().intValue());
        assertEquals(6, result.get(2).getId().intValue());
    }

    @Test
    public void getDeclarationAssigned() {
        List<FormTypeKind> result = departmentFormTypeDao.getDeclarationAssigned(2L, 'T');
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId().intValue());
    }

    @Test
    public void getByPerformerId() {
        List<Long> result = departmentFormTypeDao.getByPerformerId(2, Arrays.asList(TaxType.TRANSPORT), Arrays.asList(FormDataKind.SUMMARY));
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).intValue());
        assertEquals(11, result.get(1).intValue());
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
        List<Pair<DepartmentFormType, Pair<Date, Date>>> pairs = departmentFormTypeDao.findSourcesForFormType(2, dateStart, dateEnd);
        assertEquals(1, pairs.size());
    }

    @Test
    public void getDFTByFTTest(){
        assertEquals(3, departmentFormTypeDao.getDFTByFormType(1).size());
    }

    @Test
    public void getByPerformerIdTest(){
        assertEquals(1, departmentFormTypeDao.getDFTByPerformerId(1,
                Arrays.asList(TaxType.values()),
                Arrays.asList(FormDataKind.values())).size());
    }

    @Test
    public void getByListIdsTest(){
        assertEquals(3, departmentFormTypeDao.getByListIds(Arrays.asList(1l, 2l, 3l)).size());
    }

    @Test
    public void deleteIdsTest1(){
        List<DepartmentFormType> deps= departmentFormTypeDao.getByListIds(Arrays.asList(1L));
        assertEquals(1, deps.size());

        departmentFormTypeDao.delete(1l);

        deps= departmentFormTypeDao.getByListIds(Arrays.asList(1L));
        assertEquals(0, deps.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteEmptyIdsTest(){
        departmentFormTypeDao.delete(new ArrayList<Long>(0));
    }

    @Test
    public void deleteIdsTest2(){
        departmentFormTypeDao.delete(Arrays.asList(1l));
    }

    @Test
    public void savePerformers(){
        departmentFormTypeDao.savePerformers(6, Arrays.asList(1, 2));
        departmentFormTypeDao.deletePerformers(6);
    }
}
