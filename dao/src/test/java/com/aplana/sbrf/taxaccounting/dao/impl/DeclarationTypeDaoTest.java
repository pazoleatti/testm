package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationTypeDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DeclarationTypeDaoTest {
	@Autowired
	private DeclarationTypeDao declarationTypeDao;
	
	@Test
	public void testGet() {
		DeclarationType dt = declarationTypeDao.get(1);
		assertEquals(1, dt.getId());
		assertEquals("Вид налоговой формы 1", dt.getName());
	}
	
	@Test(expected=DaoException.class)
	public void testGetIncorrectId() {
		declarationTypeDao.get(1000);
	}
	
	@Test
	public void testListAll() {
		List<DeclarationType> list = declarationTypeDao.fetchAll();
		assertEquals(7, list.size());
	}

	@Test
    @Ignore("Не используется вид налога")
	public void testListByTaxType() {
		List<DeclarationType> list = declarationTypeDao.listAllByTaxType(TaxType.TRANSPORT);
    	assertEquals(1, list.size());
	}

    @Test
    public void save() {
        DeclarationType type = new DeclarationType();
        type.setName("testName");
        type.setStatus(VersionedObjectStatus.NORMAL);

        int id = declarationTypeDao.save(type);

        assertEquals("testName", declarationTypeDao.get(id).getName());
    }

    @Test
    @Ignore("Не используется вид налога")
    public void testGetByFilter(){
        TemplateFilter filter = new TemplateFilter();
        filter.setTaxType(TaxType.INCOME);
        Assert.assertEquals(0, declarationTypeDao.getByFilter(filter).size());
    }

    @Test
    public void testGetTypes(){
        Calendar calendar = Calendar.getInstance();
        ReportPeriod reportPeriod = new ReportPeriod();
        calendar.set(2012, Calendar.JANUARY, 1);
        reportPeriod.setStartDate(calendar.getTime());
        reportPeriod.setCalendarStartDate(calendar.getTime());
        calendar.set(2013, Calendar.DECEMBER, 31);
        reportPeriod.setEndDate(calendar.getTime());
        Assert.assertEquals(0, declarationTypeDao.getTypes(1, reportPeriod, TaxType.INCOME, Arrays.asList(DeclarationFormKind.CONSOLIDATED)).size());
    }

    @Test
    public void testUpdateDT(){
        String newName = "new_name";
        DeclarationType dt = declarationTypeDao.get(1);
        dt.setName(newName);
        declarationTypeDao.updateDT(dt);
        Assert.assertEquals(newName, declarationTypeDao.get(1).getName());
    }
}
