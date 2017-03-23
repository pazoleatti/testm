package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationDataDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DeclarationDataDaoTest {
	@Autowired
	private DeclarationDataDao declarationDataDao;

    private BlobData blobData;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @Before
    public void init(){
        blobData = new BlobData();
        blobData.setName("");
        blobData.setInputStream(new ByteArrayInputStream("test-data-string-2".getBytes()));
        blobData.setCreationDate(new Date());
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
    }
	
	@Test
	public void testGet() {
        assertTrue(declarationDataDao.existDeclarationData(1));

        DeclarationData d1 = declarationDataDao.get(1);
		assertEquals(1, d1.getId().intValue());
		assertEquals(1, d1.getDeclarationTemplateId());
        assertEquals(102, d1.getDepartmentReportPeriodId().intValue());
		assertEquals(2, d1.getReportPeriodId());
		assertEquals(1, d1.getDepartmentId());
        assertEquals("CD12", d1.getTaxOrganCode());
        assertEquals("123456789", d1.getKpp());
        assertEquals(State.ACCEPTED, d1.getState());

		DeclarationData d2 = declarationDataDao.get(2);
		assertEquals(2, d2.getId().intValue());
		assertEquals(1, d2.getDeclarationTemplateId());
        assertEquals(204 , d2.getDepartmentReportPeriodId().intValue());
		assertEquals(4, d2.getReportPeriodId());
		assertEquals(2, d2.getDepartmentId());
        assertEquals(State.CREATED, d2.getState());
	}
	
	@Test(expected=DaoException.class)
	public void testGetNotExisted() {
		declarationDataDao.get(1000l);
	}

/*
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
*/
	@Test(expected=DaoException.class)
	public void testGetDataNotExisted() {
		declarationDataDao.get(1000l);
	}

    @Test
    public void testGetDataNotExisted2() {
        assertFalse(declarationDataDao.existDeclarationData(1000l));
    }

    @Test
	public void testSetAccepted() {
		declarationDataDao.setStatus(3l, State.CREATED);
		DeclarationData d3 = declarationDataDao.get(3l);
        assertEquals(State.CREATED, d3.getState());

		declarationDataDao.setStatus(4l, State.ACCEPTED);
		DeclarationData d4 = declarationDataDao.get(4l);
        assertEquals(State.ACCEPTED, d4.getState());
	}

	@Test(expected=DaoException.class)
	public void testSetAcceptedNotExistsed() {
		declarationDataDao.setStatus(1000l, State.ACCEPTED);
	}

	@Test
	public void testDelete() {
		declarationDataDao.delete(5);
		try {
			declarationDataDao.get(5);
			fail("Record was not deleted");
		} catch (DaoException e) {
            //Nothing
		}
	}

	@Test(expected=DaoException.class)
	public void testDeleteNotExisted() {
		declarationDataDao.delete(1000l);
	}

	@Test
	public void testSaveNew() {
        String taxOrganCode = "G55";
        String kpp = "567898678";
		DeclarationData d = new DeclarationData();
        d.setState(State.ACCEPTED);
		d.setDeclarationTemplateId(1);
        d.setDepartmentReportPeriodId(220);
        d.setTaxOrganCode(taxOrganCode);
        d.setKpp(kpp);

		long id = declarationDataDao.saveNew(d);

		DeclarationData d2 = declarationDataDao.get(id);
		assertEquals(1, d2.getDeclarationTemplateId());
        assertEquals(220, d2.getDepartmentReportPeriodId().intValue());
		assertEquals(2, d2.getDepartmentId());
		assertEquals(20, d2.getReportPeriodId());
        assertEquals(State.ACCEPTED, d2.getState());
        Assert.assertEquals(taxOrganCode, d2.getTaxOrganCode());
        Assert.assertEquals(kpp, d2.getKpp());
	}

	@Test(expected=DaoException.class)
	public void testSaveNewWithId() {
		DeclarationData d = new DeclarationData();
		d.setId(1000l);
        d.setState(State.ACCEPTED);
		d.setDeclarationTemplateId(1);
        d.setDepartmentReportPeriodId(111);
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
    public void findPageByFilterTest() {
        DeclarationDataFilter filter = new DeclarationDataFilter();
        assertArrayEquals(new Long[]{7l, 5l, 4l, 3l, 2l, 1l}, declarationDataDao.findIdsByFilter(filter, DeclarationDataSearchOrdering.ID, false).toArray());
        assertArrayEquals(new Long[]{1l, 2l, 3l, 4l, 5l, 7l}, declarationDataDao.findIdsByFilter(filter, DeclarationDataSearchOrdering.ID, true).toArray());
    }

	@Test
	public void findPageSortingTest() {
		DeclarationDataFilter filter = new DeclarationDataFilter();
		PagingParams pageParams = new PagingParams(0, 5);

		PagingResult<DeclarationDataSearchResultItem> res;

		res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.ID, true, pageParams);
        assertIdsEquals(new long[]{1, 2, 3, 4, 5}, res);
        res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.ID, false, pageParams);
        assertIdsEquals(new long[]{7, 5, 4, 3, 2}, res);

        res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.REPORT_PERIOD_NAME, true, pageParams);
        assertIdsEquals(new long[]{2, 7, 3, 4, 5}, res);
        res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.REPORT_PERIOD_NAME, false, pageParams);
        assertIdsEquals(new long[]{1, 5, 4, 3, 7}, res);

        res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.DEPARTMENT_NAME, true, pageParams);
        assertIdsEquals(new long[]{1, 3, 2, 4, 5}, res);
        res = declarationDataDao.findPage(filter, DeclarationDataSearchOrdering.DEPARTMENT_NAME, false, pageParams);
        assertIdsEquals(new long[]{7, 5, 4, 2, 3}, res);
	}

    @Test
    public void findTest() {
        DeclarationData declaration = declarationDataDao.find(1, 204, null, null, null, null, null);
        assertEquals(2, declaration.getId().intValue());
    }

    @Test(expected = DaoException.class)
    public void findKpp1Test() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setDepartmentReportPeriodId(102);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setKpp("123456789");
        declarationData.setTaxOrganCode("CD11");
        declarationData.setState(State.CREATED);
        declarationDataDao.saveNew(declarationData);

        DeclarationData declaration = declarationDataDao.find(1, 102, "123456789", null, null, null, null);
        assertNotNull(declaration);
        assertEquals(1, declaration.getId().intValue());
    }

    @Test(expected = DaoException.class)
    public void findKpp2Test() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setDepartmentReportPeriodId(102);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setKpp("123456789");
        declarationData.setTaxOrganCode("CD11");
        declarationData.setState(State.CREATED);
        declarationDataDao.saveNew(declarationData);

        DeclarationData declaration = declarationDataDao.find(1, 102, null, null, null, null, null);
        assertNotNull(declaration);
        assertEquals(1, declaration.getId().intValue());
    }

    @Test
    public void findKpp3Test() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setDepartmentReportPeriodId(102);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setKpp("123456789");
        declarationData.setTaxOrganCode("CD11");
        declarationData.setState(State.CREATED);
        declarationDataDao.saveNew(declarationData);

        DeclarationData declaration = declarationDataDao.find(1, 102, "123456789", null, "CD12", null, null);
        assertNotNull(declaration);
        assertEquals(1, declaration.getId().intValue());
        declaration = declarationDataDao.find(1, 102, null, null, "CD12", null, null);
        assertNotNull(declaration);
        assertEquals(1, declaration.getId().intValue());
    }

    @Test
    public void findEmptyResultTest() {
        DeclarationData declaration = declarationDataDao.find(222, 222, null, null, null, null, null);
        assertNull(declaration);
    }

    @Test
    public void getDeclarationIdsTest(){
        Assert.assertEquals(Arrays.asList(1L, 3L), declarationDataDao.getDeclarationIds(1, 1));
        Assert.assertEquals(new ArrayList<Long>(), declarationDataDao.getDeclarationIds(222, 222));
    }

    @Test
    public void testFindDeclarationDataByFormTemplate() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        Assert.assertEquals(6, declarationDataDao.findDeclarationDataByFormTemplate(1, format.parse("2013.01.01")).size());
    }

    @Test
    public void findAllDeclarationData() {
        List<DeclarationData> declarationDataList = declarationDataDao.findAllDeclarationData(1, 1, 20);
        Assert.assertNotNull(declarationDataList);
        Assert.assertEquals(1, declarationDataList.size());
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
		}
	}

    @Test
    public void getLastTest() {
        DeclarationData declarationData = declarationDataDao.getLast(1, 1, 20);
        Assert.assertNotNull(declarationData);
        Assert.assertEquals(3, declarationData.getId().intValue());
    }

    @Test
    public void findFormDataIdsByIntersectionInReportPeriodTest() throws ParseException {
        Assert.assertEquals(6, declarationDataDao.findDDIdsByRangeInReportPeriod(1,
                SIMPLE_DATE_FORMAT.parse("01.01.2012"), SIMPLE_DATE_FORMAT.parse("31.12.2012")).size());
    }

    @Test
    public void testNote() {
        assertEquals(declarationDataDao.getNote(1), "Первичка по");
        assertNull(declarationDataDao.getNote(7));

        declarationDataDao.updateNote(7, "Проверка комментария к НФ(decl)");
        assertEquals(declarationDataDao.getNote(7), "Проверка комментария к НФ(decl)");
    }

    @Test
    public void testFindDeclarationDataByFileNameAndFileType() {
        declarationDataDao.findDeclarationDataByFileNameAndFileType("fileName", -1L);
    }

    @Test
    public void testSetDocStateId() {
        declarationDataDao.setDocStateId(1L, 268558099L);
    }
}
