package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationDataDaoTest.xml"})
@Transactional
public class DeclarationDataDaoTest {
	@Autowired
	private DeclarationDataDao declarationDataDao;
	
	@Test
	public void testGet() {
		DeclarationData d1 = declarationDataDao.get(1);
		assertEquals(1, d1.getId().longValue());
		assertEquals(1, d1.getDeclarationTemplateId());
		assertEquals(1, d1.getReportPeriodId());
		assertEquals(2, d1.getDepartmentId());
		assertTrue(d1.isAccepted());

		DeclarationData d2 = declarationDataDao.get(2);
		assertEquals(2, d2.getId().longValue());
		assertEquals(1, d2.getDeclarationTemplateId());
		assertEquals(2, d2.getReportPeriodId());
		assertEquals(4, d2.getDepartmentId());
		assertFalse(d2.isAccepted());
		
	}
	
	@Test(expected=DaoException.class)
	public void testGetNotExisted() {
		declarationDataDao.get(1000l);
	}
	
	@Test
	public void testHasXmlData() {
		assertTrue(declarationDataDao.hasXmlData(1));
		assertFalse(declarationDataDao.hasXmlData(2));
	}
	
	@Test
	public void testGetData() {
		String data = declarationDataDao.getXmlData(1);
		assertEquals("test-data-string-1", data);
	}
	
	@Test
	public void testGetDataEmpty() {
		String data = declarationDataDao.getXmlData(2);
		assertNull(data);
	}
	
	
	@Test(expected=DaoException.class)
	public void testGetDataNotExisted() {
		declarationDataDao.getXmlData(1000l);
	}
	
	@Test
	public void testSetAccepted() {
		declarationDataDao.setAccepted(3l, false);
		DeclarationData d3 = declarationDataDao.get(3l);
		assertFalse(d3.isAccepted());
		
		declarationDataDao.setAccepted(4l, true);
		DeclarationData d4 = declarationDataDao.get(4l);
		assertTrue(d4.isAccepted());
	}
	
	@Test(expected=DaoException.class)
	public void testSetAcceptedNotExistsed() {
		declarationDataDao.setAccepted(1000l, true);
	}

	@Test
	public void testDelete() {
		declarationDataDao.delete(5);
		try {
			declarationDataDao.get(5);
			fail("Record was not deleted");
		} catch (DaoException e) {
		}
	}
	
	@Test(expected=DaoException.class)
	public void testDeleteNotExisted() {
		declarationDataDao.delete(1000l);
	}
	
	@Test
	public void testSaveNew() {
		DeclarationData d = new DeclarationData();
		d.setAccepted(true);
		d.setDeclarationTemplateId(1);
		d.setDepartmentId(1);
		d.setReportPeriodId(6);
		
		long id = declarationDataDao.saveNew(d);

		DeclarationData d2 = declarationDataDao.get(id);
		assertEquals(1, d2.getDeclarationTemplateId());
		assertEquals(1, d2.getDepartmentId());
		assertEquals(6, d2.getReportPeriodId());
		assertTrue(d2.isAccepted());
	}
	
	@Test(expected=DaoException.class)
	public void testSaveNewWithId() {
		DeclarationData d = new DeclarationData();
		d.setId(1000l);
		d.setAccepted(true);
		d.setDeclarationTemplateId(1);
		d.setDepartmentId(1);
		d.setReportPeriodId(6);		
		declarationDataDao.saveNew(d);
	}
	
	
	@Test
	public void saveXmlData() {
		declarationDataDao.setXmlData(7, "test-data-string-2");
		String data = declarationDataDao.getXmlData(7);
		assertEquals("test-data-string-2", data);
	}
	
	@Test
	public void saveReportPdfData() {
		declarationDataDao.setPdfData(7, "test-data-string-2".getBytes());
		byte[] data = declarationDataDao.getPdfData(7);
		Assert.assertArrayEquals("test-data-string-2".getBytes(), data);
	}
	
	@Test
	public void saveReportXlsxData() {
		declarationDataDao.setXlsxData(7, "test-data-string-2".getBytes());
		byte[] data = declarationDataDao.getXlsxData(7);
		Assert.assertArrayEquals("test-data-string-2".getBytes(), data);
	}

	@Test
	public void findPageTest(){
		DeclarationDataFilter filter = new DeclarationDataFilter();
		PagingParams pageParams = new PagingParams(0, 0);
		PagingResult<DeclarationDataSearchResultItem> res;
		final long TOTAL_RECORDS_COUNT = declarationDataDao.getCount(filter);

		for(int requestedCount = 0; requestedCount < TOTAL_RECORDS_COUNT; requestedCount += 2){
			pageParams.setStartIndex(0);
			pageParams.setCount(requestedCount);
			res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.ID, true, pageParams);
			assertEquals(requestedCount, res.getRecords().size());
			assertEquals(TOTAL_RECORDS_COUNT, res.getTotalRecordCount());
		}
	}

	@Test
	public void findPageSortingTest() {
		DeclarationDataFilter filter = new DeclarationDataFilter();
		PagingParams pageParams = new PagingParams(0, 5);

		PagingResult<DeclarationDataSearchResultItem> res;

		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.ID, true, pageParams);
		assertIdsEquals(new long[]{1, 2, 3, 4, 5}, res.getRecords());
		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.ID, false, pageParams);
		assertIdsEquals(new long[] {7, 5, 4, 3, 2}, res.getRecords());


		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.REPORT_PERIOD_NAME, true, pageParams);
		assertIdsEquals(new long[]{3, 1, 2, 4, 5}, res.getRecords());
		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.REPORT_PERIOD_NAME, false, pageParams);
		assertIdsEquals(new long[] {7, 5, 4, 2, 1}, res.getRecords());

		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.DEPARTMENT_NAME, true, pageParams);
		assertIdsEquals(new long[]{4, 3, 2, 5, 7}, res.getRecords());
		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.DEPARTMENT_NAME, false, pageParams);
		assertIdsEquals(new long[] {1, 7, 5, 2, 3}, res.getRecords());

	}

	@Test
	public void findTest() {
		DeclarationData declaration = declarationDataDao.find(1, 2, 1);
		assertEquals(1, declaration.getId().longValue());
	}

	@Test
	public void findEmptyResultTest() {
		DeclarationData declaration = declarationDataDao.find(222, 222, 222);
		assertNull(declaration);
	}

	private void assertIdsEquals(long[] expected, List<DeclarationDataSearchResultItem> items) {
		if (expected.length != items.size()) {
			fail("List size mismatch: " + expected.length + " expected but " + items.size() + " received");
			return;
		}

		long[] received = new long[expected.length];

		boolean failed = false;
		for (int i = 0; i < expected.length; ++i) {
			DeclarationDataSearchResultItem item = items.get(i);
			received[i] = item.getDeclarationDataId();
			if (received[i] != expected[i]) {
				failed = true;
			}
		}

		if (failed) {
			fail("Wrong list of ids: " + expected + " expected but " + received + " received");
		}
	}
}
