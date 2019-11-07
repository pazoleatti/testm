package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.DeclarationFormKind.CONSOLIDATED;
import static com.aplana.sbrf.taxaccounting.model.DeclarationFormKind.PRIMARY;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

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
    public void init() {
        ReflectionTestUtils.setField(ReflectionTestUtils.getField(SqlUtils.class, "repository"),
                "namedParameterJdbcTemplate", ReflectionTestUtils.getField(declarationDataDao, "namedParameterJdbcTemplate"));
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
        assertEquals(204, d2.getDepartmentReportPeriodId().intValue());
        assertEquals(4, d2.getReportPeriodId());
        assertEquals(2, d2.getDepartmentId());
        assertEquals(State.CREATED, d2.getState());
    }

    @Test(expected = DaoException.class)
    public void testGetNotExisted() {
        declarationDataDao.get(1000L);
    }

    @Test(expected = DaoException.class)
    public void testGetDataNotExisted() {
        declarationDataDao.get(1000L);
    }

    @Test
    public void testGetDataNotExisted2() {
        assertFalse(declarationDataDao.existDeclarationData(1000L));
    }

    @Test
    public void testSetAccepted() {
        declarationDataDao.setStatus(3L, State.CREATED);
        DeclarationData d3 = declarationDataDao.get(3L);
        assertEquals(State.CREATED, d3.getState());

        declarationDataDao.setStatus(4L, State.ACCEPTED);
        DeclarationData d4 = declarationDataDao.get(4L);
        assertEquals(State.ACCEPTED, d4.getState());
    }

    @Test(expected = DaoException.class)
    public void testSetAcceptedNotExistsed() {
        declarationDataDao.setStatus(1000L, State.ACCEPTED);
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

    @Test(expected = DaoException.class)
    public void testDeleteNotExisted() {
        declarationDataDao.delete(1000L);
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
        d.setCreatedBy(createUser());

        declarationDataDao.create(d);

        DeclarationData d2 = declarationDataDao.get(d.getId());
        assertEquals(1, d2.getDeclarationTemplateId());
        assertEquals(220, d2.getDepartmentReportPeriodId().intValue());
        assertEquals(2, d2.getDepartmentId());
        assertEquals(20, d2.getReportPeriodId());
        assertEquals(State.ACCEPTED, d2.getState());
        Assert.assertEquals(taxOrganCode, d2.getTaxOrganCode());
        Assert.assertEquals(kpp, d2.getKpp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNewWithId() {
        DeclarationData d = new DeclarationData();
        d.setId(1000L);
        d.setState(State.ACCEPTED);
        d.setDeclarationTemplateId(1);
        d.setDepartmentReportPeriodId(111);
        declarationDataDao.create(d);
    }

    @Test
    public void findPageByFilterTest() {
        DeclarationDataFilter filter = new DeclarationDataFilter();
        assertArrayEquals(new Long[]{123L, 71L, 70L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L}, declarationDataDao.findIdsByFilter(filter, DeclarationDataSearchOrdering.ID, false).toArray());
        assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 70L, 71L, 123L}, declarationDataDao.findIdsByFilter(filter, DeclarationDataSearchOrdering.ID, true).toArray());
    }

    private PagingParams getPagingParams(int page, int count) {
        PagingParams pagingParams = PagingParams.getInstance(page, count);
        pagingParams.setProperty("declarationDataId");
        pagingParams.setDirection("ASC");
        return pagingParams;
    }

    @Test
    public void findPage_all() {
        DeclarationDataFilter filter = new DeclarationDataFilter();
        PagingResult<DeclarationDataJournalItem> page = declarationDataDao.findPage(filter, getPagingParams(1, Integer.MAX_VALUE));
        assertEquals(11, page.size());
        assertEquals(11, page.getTotalCount());
    }

    @Test
    public void findPage_2thPage() {
        DeclarationDataFilter filter = new DeclarationDataFilter();
        PagingResult<DeclarationDataJournalItem> page = declarationDataDao.findPage(filter, getPagingParams(2, 2));
        assertEquals(2, page.size());
        assertEquals(11, page.getTotalCount());
        assertEquals(new Long(3), page.get(0).getDeclarationDataId());
        assertEquals(new Long(4), page.get(1).getDeclarationDataId());
    }

    @Test
    public void findPage_filter1() {
        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDeclarationDataId(2L);
        filter.setFormStates(Arrays.asList(State.ACCEPTED.getId()));
        filter.setFileName("ilenam");
        filter.setTaxOrganKpp("456");
        filter.setOktmo("ktm");
        filter.setTaxOrganCode("d1");
        filter.setNote("ервичка п");
        PagingResult<DeclarationDataJournalItem> page = declarationDataDao.findPage(filter, getPagingParams(1, Integer.MAX_VALUE));
        assertEquals(1, page.size());
        assertEquals(123L, page.get(0).getDeclarationDataId().longValue());
    }

    @Test
    public void findPage_filter2() {
        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(asList(1, 2));
        filter.setFormKindIds(asList(PRIMARY.getId(), CONSOLIDATED.getId()));
        filter.setDeclarationTypeIds(asList(1L, 2L));
        filter.setReportPeriodIds(asList(20, 3));
        PagingResult<DeclarationDataJournalItem> page = declarationDataDao.findPage(filter, getPagingParams(1, Integer.MAX_VALUE));
        assertEquals(1, page.size());
        assertEquals(3L, page.get(0).getDeclarationDataId().longValue());
        assertEquals("Первичная", page.get(0).getDeclarationKind());
        assertEquals("Вид налоговой формы 1", page.get(0).getDeclarationType());
        assertEquals("Банк", page.get(0).getDepartment());
        assertEquals("Принята", page.get(0).getState());
        assertEquals("Контролёр Банка", page.get(0).getCreationUserName());
        assertEquals("2014: первый квартал (корр. 02.01.2014): 6 НДФЛ", page.get(0).getReportPeriod());
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
        declarationDataDao.create(declarationData);

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
        declarationDataDao.create(declarationData);

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
        declarationData.setCreatedBy(createUser());
        declarationDataDao.create(declarationData);

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
    public void findKnfByKnfTypeAndPeriodIdTest() {
        DepartmentReportPeriodFilter drpFilter = new DepartmentReportPeriodFilter();
        drpFilter.setDepartmentId(1);
        drpFilter.setYearStart(2016);
        drpFilter.setYearEnd(2016);
        drpFilter.setDictTaxPeriodId(24L); // report_period_type

        DeclarationData knf = declarationDataDao.findKnfByKnfTypeAndPeriodFilter(RefBookKnfType.ALL, drpFilter);
        assertNotNull(knf);
        assertEquals(6, (long) knf.getId());
    }

    @Test
    public void checkExistingConsolidateDeclarationInYearPeriodAndPeriodCodeTest() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setDeclarationTemplateId(DeclarationType.NDFL_CONSOLIDATE);
        declarationData.setKnfType(RefBookKnfType.ALL);

        List<Long> existingDeclarationsIds =
                declarationDataDao.findExistingDeclarationsForCreationCheck(declarationData, 1000, "33", null);

        assertEquals(1, existingDeclarationsIds.size());
        assertEquals(new Long(6), existingDeclarationsIds.get(0));

    }

    @Test
    public void checkExistingConsolidateDeclarationInYearReorgPeriodAndPeriodCodeTest() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setDeclarationTemplateId(DeclarationType.NDFL_CONSOLIDATE);
        declarationData.setKnfType(RefBookKnfType.ALL);

        List<Long> existingDeclarationsIds =
                declarationDataDao.findExistingDeclarationsForCreationCheck(declarationData, 1000, "90", null);

        assertEquals(1, existingDeclarationsIds.size());
        assertEquals(new Long(7), existingDeclarationsIds.get(0));
    }

    @Test
    public void checkExistingConsolidatedDeclarationInCorrectionPeriodTest() throws Exception {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setDeclarationTemplateId(DeclarationType.NDFL_CONSOLIDATE);
        declarationData.setKnfType(RefBookKnfType.ALL);

        Date correctionDate = SIMPLE_DATE_FORMAT.parse("01.01.2018");
        List<Long> existingDeclarationsIds =
                declarationDataDao.findExistingDeclarationsForCreationCheck(declarationData, 1000, "31", correctionDate);

        assertEquals(1, existingDeclarationsIds.size());
        assertEquals(new Long(71), existingDeclarationsIds.get(0));
    }

    @Test
    public void getDeclarationIdsTest() {
        Assert.assertEquals(asList(1L, 3L), declarationDataDao.getDeclarationIds(1, 1));
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
    public void findFormDataIdsByIntersectionInReportPeriodTest() throws ParseException {
        Assert.assertEquals(6, declarationDataDao.findDDIdsByRangeInReportPeriod(1,
                SIMPLE_DATE_FORMAT.parse("01.01.2012"), SIMPLE_DATE_FORMAT.parse("31.12.2012")).size());
    }

    @Test
    public void testNote() {
        assertEquals(declarationDataDao.getNote(1), "Первичка по");
        assertNull(declarationDataDao.getNote(2));

        declarationDataDao.updateNote(123, "Проверка комментария к НФ(decl)");
        assertEquals(declarationDataDao.getNote(123), "Проверка комментария к НФ(decl)");
    }

    @Test
    public void testFindDeclarationDataByFileNameAndFileType() {
        declarationDataDao.findDeclarationDataByFileNameAndFileType("fileName", -1L);
    }

    @Test
    public void testSetDocStateId() {
        declarationDataDao.setDocStateId(1L, 268558099L);
    }

    @Test
    public void getSaveDeclarationDataKppList() {
        assertEquals(Collections.emptyList(), declarationDataDao.getDeclarationDataKppList(1L));
        declarationDataDao.createDeclarationDataKppList(1L, Sets.newHashSet("1", "2", "3"));
        assertEquals(asList("1", "2", "3"), declarationDataDao.getDeclarationDataKppList(1L));
    }

    @Test
    public void testFindONFFor2NDFL() {
        List<DeclarationData> declarationDataList = declarationDataDao.findONFFor2Ndfl(104, "33", 2018, "123456789", "12345678");
        assertEquals(new Long(8), declarationDataList.get(0).getId());
    }

    @Test
    public void isKnfTest() {
        assertTrue(declarationDataDao.isKnf(6L));
        assertFalse(declarationDataDao.isKnf(8L));
    }

    private TAUser createUser() {
        TAUser user = new TAUser();
        user.setId(1);
        return user;
    }
}
