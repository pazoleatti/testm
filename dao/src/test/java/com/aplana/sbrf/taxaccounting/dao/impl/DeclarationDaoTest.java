package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Declaration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationDaoTest.xml"})
@Transactional
public class DeclarationDaoTest {
	@Autowired
	private DeclarationDao declarationDao;
	
	@Test
	public void testGet() {
		Declaration d1 = declarationDao.get(1);
		assertEquals(1, d1.getId().longValue());
		assertEquals(1, d1.getDeclarationTemplateId());
		assertEquals(1, d1.getReportPeriodId());
		assertEquals(1, d1.getDepartmentId());
		assertTrue(d1.isAccepted());
		
		Declaration d2 = declarationDao.get(2);
		assertEquals(2, d2.getId().longValue());
		assertEquals(1, d2.getDeclarationTemplateId());
		assertEquals(2, d2.getReportPeriodId());
		assertEquals(1, d2.getDepartmentId());
		assertFalse(d2.isAccepted());
		
	}
	
	@Test(expected=DaoException.class)
	public void testGetNotExisted() {
		declarationDao.get(1000l);
	}
	
	@Test
	public void testGetData() {
		String data = declarationDao.getXmlData(1);
		assertEquals("test-data-string-1", data);
	}
	
	@Test
	public void testGetDataEmpty() {
		String data = declarationDao.getXmlData(2);
		assertNull(data);
	}
	
	
	@Test(expected=DaoException.class)
	public void testGetDataNotExisted() {
		declarationDao.getXmlData(1000l);
	}
	
	@Test
	public void testSetAccepted() {
		declarationDao.setAccepted(3l, false);
		Declaration d3 = declarationDao.get(3l);
		assertFalse(d3.isAccepted());
		
		declarationDao.setAccepted(4l, true);
		Declaration d4 = declarationDao.get(4l);
		assertTrue(d4.isAccepted());
	}
	
	@Test(expected=DaoException.class)
	public void testSetAcceptedNotExistsed() {
		declarationDao.setAccepted(1000l, true);
	}

	@Test
	public void testDelete() {
		declarationDao.delete(5);
		try {
			declarationDao.get(5);
			fail("Record was not deleted");
		} catch (DaoException e) {
		}
	}
	
	@Test(expected=DaoException.class)
	public void testDeleteNotExisted() {
		declarationDao.delete(1000l);
	}
	
	@Test
	public void testSaveNew() {
		Declaration d = new Declaration();
		d.setAccepted(true);
		d.setDeclarationTemplateId(1);
		d.setDepartmentId(1);
		d.setReportPeriodId(6);
		
		long id = declarationDao.saveNew(d);

		Declaration d2 = declarationDao.get(id);
		assertEquals(1, d2.getDeclarationTemplateId());
		assertEquals(1, d2.getDepartmentId());
		assertEquals(6, d2.getReportPeriodId());
		assertTrue(d2.isAccepted());
	}
	
	@Test(expected=DaoException.class)
	public void testSaveNewWithId() {
		Declaration d = new Declaration();
		d.setId(1000l);
		d.setAccepted(true);
		d.setDeclarationTemplateId(1);
		d.setDepartmentId(1);
		d.setReportPeriodId(6);		
		declarationDao.saveNew(d);
	}
	
	
	@Test
	public void testSaveData() {
		declarationDao.setXmlData(7, "test-data-string-2");
		String data = declarationDao.getXmlData(7);
		assertEquals("test-data-string-2", data);
	}
	
}
