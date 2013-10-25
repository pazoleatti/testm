package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationDataDaoTest.xml"})
@Transactional
public class DeclarationDataDaoTest {
	@Autowired
	private DeclarationDataDao declarationDataDao;

    @Autowired
    private BlobDataDao blobDataDao;
    private  BlobData blobData;

    @Before
    public void init(){
        blobData = new BlobData();
        blobData.setName("");
        blobData.setDataSize(10);
        blobData.setInputStream(new ByteArrayInputStream("test-data-string-2".getBytes()));
        blobData.setCreationDate(new Date());
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setType(0);
    }
	
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
        assertFalse(declarationDataDao.hasXmlData(1));
		assertFalse(declarationDataDao.hasXmlData(2));
	}
	
	@Test
	public void testGetData() {
		String data = declarationDataDao.get(1).getXmlDataUuid();
		assertNull(data);
	}
	
	@Test
	public void testGetDataEmpty() {
		String data = declarationDataDao.get(2).getXmlDataUuid();
		assertNull(data);
	}
	
	
	@Test(expected=DaoException.class)
	public void testGetDataNotExisted() {
		declarationDataDao.get(1000l);
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
	public void findPageTest(){
		DeclarationDataFilter filter = new DeclarationDataFilter();
		PagingParams pageParams = new PagingParams(0, 0);
		PagingResult<DeclarationDataSearchResultItem> res;
		final long TOTAL_RECORDS_COUNT = declarationDataDao.getCount(filter);

		for(int requestedCount = 0; requestedCount < TOTAL_RECORDS_COUNT; requestedCount += 2){
			pageParams.setStartIndex(0);
			pageParams.setCount(requestedCount);
			res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.ID, true, pageParams);
			assertEquals(requestedCount, res.size());
			assertEquals(TOTAL_RECORDS_COUNT, res.getTotalCount());
		}
	}

	@Test
	public void findPageSortingTest() {
		DeclarationDataFilter filter = new DeclarationDataFilter();
		PagingParams pageParams = new PagingParams(0, 5);

		PagingResult<DeclarationDataSearchResultItem> res;

		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.ID, true, pageParams);
		assertIdsEquals(new long[]{1, 2, 3, 4, 5}, res);
		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.ID, false, pageParams);
		assertIdsEquals(new long[] {7, 5, 4, 3, 2}, res);


		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.REPORT_PERIOD_NAME, true, pageParams);
		assertIdsEquals(new long[]{3, 1, 2, 4, 5}, res);
		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.REPORT_PERIOD_NAME, false, pageParams);
		assertIdsEquals(new long[] {7, 5, 4, 2, 1}, res);

		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.DEPARTMENT_NAME, true, pageParams);
		assertIdsEquals(new long[]{4, 3, 2, 5, 7}, res);
		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.DEPARTMENT_NAME, false, pageParams);
		assertIdsEquals(new long[] {1, 7, 5, 2, 3}, res);

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

    @Test
    public void updateTest(){
        DeclarationData declarationDataOld = declarationDataDao.get(1);
        DeclarationData declarationDataNew = new DeclarationData();
        declarationDataNew.setId(declarationDataOld.getId());

        String xmlId = blobDataDao.create(blobData);
        declarationDataNew.setXmlDataUuid(xmlId);

        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        String pdfId = blobDataDao.create(blobData);
        declarationDataNew.setPdfDataUuid(pdfId);

        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        String xlsxId = blobDataDao.create(blobData);
        declarationDataNew.setXlsxDataUuid(xlsxId);

        declarationDataDao.update(declarationDataNew);
        assertNotEquals(declarationDataOld.getXmlDataUuid(), declarationDataNew.getXmlDataUuid());
        assertNotEquals(declarationDataOld.getPdfDataUuid(), declarationDataNew.getPdfDataUuid());
        assertNotEquals(declarationDataOld.getXlsxDataUuid(), declarationDataNew.getXlsxDataUuid());
        assertEquals(blobData.getUuid(), declarationDataDao.get(1).getXlsxDataUuid());
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
			fail("Wrong list of ids: " + Arrays.toString(expected) + " expected but " + Arrays.toString(received) + " received");
			fail("Wrong list of ids: " + Arrays.toString(expected) + " expected but " + Arrays.toString(received) + " received");
		}
	}
}
