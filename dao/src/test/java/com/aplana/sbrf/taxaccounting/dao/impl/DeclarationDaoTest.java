package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

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
		assertEquals(2, d1.getDepartmentId());
		assertTrue(d1.isAccepted());

		Declaration d2 = declarationDao.get(2);
		assertEquals(2, d2.getId().longValue());
		assertEquals(1, d2.getDeclarationTemplateId());
		assertEquals(2, d2.getReportPeriodId());
		assertEquals(4, d2.getDepartmentId());
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

	@Test
	public void findPageTest(){
		DeclarationFilter filter = new DeclarationFilter();
		PaginatedSearchParams pageParams = new PaginatedSearchParams(0, 0);
		PaginatedSearchResult<DeclarationSearchResultItem> res;
		final long TOTAL_RECORDS_COUNT = declarationDao.getCount(filter);

		for(int requestedCount = 0; requestedCount < TOTAL_RECORDS_COUNT; requestedCount += 2){
			pageParams.setStartIndex(0);
			pageParams.setCount(requestedCount);
			res = declarationDao.findPage(filter, DeclarationSearchOrdering.ID, true, pageParams);
			assertEquals(requestedCount, res.getRecords().size());
			assertEquals(TOTAL_RECORDS_COUNT, res.getTotalRecordCount());
		}
	}

	@Test
	public void findPageSortingTest() {
		DeclarationFilter filter = new DeclarationFilter();
		PaginatedSearchParams pageParams = new PaginatedSearchParams(0, 5);

		PaginatedSearchResult<DeclarationSearchResultItem> res;

		res = declarationDao.findPage(filter, DeclarationSearchOrdering.ID, true, pageParams);
		assertIdsEquals(new long[]{1, 2, 3, 4, 5}, res.getRecords());
		res = declarationDao.findPage(filter, DeclarationSearchOrdering.ID, false, pageParams);
		assertIdsEquals(new long[] {7, 5, 4, 3, 2}, res.getRecords());


		res = declarationDao.findPage(filter, DeclarationSearchOrdering.REPORT_PERIOD_NAME, true, pageParams);
		assertIdsEquals(new long[]{3, 1, 2, 4, 5}, res.getRecords());
		res = declarationDao.findPage(filter, DeclarationSearchOrdering.REPORT_PERIOD_NAME, false, pageParams);
		assertIdsEquals(new long[] {7, 5, 4, 2, 1}, res.getRecords());

		res = declarationDao.findPage(filter, DeclarationSearchOrdering.DEPARTMENT_NAME, true, pageParams);
		assertIdsEquals(new long[]{4, 3, 2, 5, 7}, res.getRecords());
		res = declarationDao.findPage(filter, DeclarationSearchOrdering.DEPARTMENT_NAME, false, pageParams);
		assertIdsEquals(new long[] {1, 7, 5, 2, 3}, res.getRecords());

	}

	@Test
	public void findTest() {
		Declaration declaration = declarationDao.find(1, 2, 1);
		assertEquals(1, declaration.getId().longValue());
	}

	@Test
	public void findEmptyResultTest() {
		Declaration declaration = declarationDao.find(222, 222, 222);
		assertNull(declaration);
	}

	private void assertIdsEquals(long[] expected, List<DeclarationSearchResultItem> items) {
		if (expected.length != items.size()) {
			fail("List size mismatch: " + expected.length + " expected but " + items.size() + " received");
			return;
		}

		long[] received = new long[expected.length];

		boolean failed = false;
		for (int i = 0; i < expected.length; ++i) {
			DeclarationSearchResultItem item = items.get(i);
			received[i] = item.getDeclarationId();
			if (received[i] != expected[i]) {
				failed = true;
			}
		}

		if (failed) {
			fail("Wrong list of ids: " + expected + " expected but " + received + " received");
		}
	}
	
}
