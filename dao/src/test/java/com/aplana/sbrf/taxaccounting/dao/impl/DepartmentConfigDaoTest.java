package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPairFilter;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookOktmo;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentConfigDaoTest.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DepartmentConfigDaoTest {

    @Autowired
    private DepartmentConfigDao departmentConfigDao;

    @Test
    public void getTest() {
        assertNotNull(departmentConfigDao.findById(10));
    }

    @Test
    public void findPrevTest() {
        DepartmentConfig departmentConfig = departmentConfigDao.findPrev(DepartmentConfig.builder()
                .kpp("000000003").oktmo(new RefBookOktmo(1L))
                .startDate(new LocalDate(2018, 1, 1).toDate())
                .build());
        assertThat(departmentConfig.getId(), is(12L));
    }

    @Test
    public void findKppOktmoPairsTest() {
        List<Pair<String, String>> kppOktmoPairs = departmentConfigDao.findKppOktmoPairs(asList(2, 3), new LocalDate(2017, 1, 1).toDate());
        assertThat(kppOktmoPairs.size(), is(6));
        assertThat(kppOktmoPairs, is(Matchers.containsInAnyOrder(
                new Pair("000000002", "111"), new Pair("000000004", "111"), new Pair("000000005", "111"),
                new Pair("000000006", "111"), new Pair("000000010", "222"), new Pair("000000011", "222")
        )));
    }

    @Test
    public void findAllKppByFilterTest() {
        PagingResult<KppSelect> kppSelectList = departmentConfigDao.findAllKppByDepartmentIdAndKpp(2, null, PagingParams.getInstance(1, 10));
        assertThat(kppSelectList.size(), is(6));

        kppSelectList = departmentConfigDao.findAllKppByDepartmentIdAndKpp(2, null, PagingParams.getInstance(2, 2));
        assertThat(kppSelectList.size(), is(2));

        kppSelectList = departmentConfigDao.findAllKppByDepartmentIdAndKpp(3, "01", PagingParams.getInstance(1, 10));
        assertThat(kppSelectList.size(), is(2));
    }

    @Test
    public void findByKppAndOktmoAndDateTest() {
        DepartmentConfig departmentConfig = departmentConfigDao.findByKppAndOktmoAndDate("000000003", "111", new LocalDate(2018, 1, 1).toDate());
        assertThat(departmentConfig.getId(), is(122L));
    }

    @Test(expected = DaoException.class)
    public void findByKppAndOktmoAndDateTest2() {
        DepartmentConfig departmentConfig = departmentConfigDao.findByKppAndOktmoAndDate("000000003", "111", new LocalDate(2017, 1, 1).toDate());
        assertThat(departmentConfig.getId(), is(nullValue()));
    }

    @Test
    public void findAllByDeclarationTest() {
        List<Pair<KppOktmoPair, DepartmentConfig>> results = departmentConfigDao.findAllByDeclaration(
                declarationData(1, 2, 2), new LocalDate(2018, 1, 1).toDate());
        assertThat(results.size(), is(6));
        for (Pair<KppOktmoPair, DepartmentConfig> result : results) {
            String kpp = result.getFirst() != null ? result.getFirst().getKpp() : result.getSecond().getKpp();
            if (asList("000000001", "000000006").contains(kpp)) {
                assertThat(result.getFirst(), is(notNullValue()));
                assertThat(result.getSecond(), is(nullValue()));
            } else if (singletonList("000000005").contains(kpp)) {
                assertThat(result.getFirst(), is(nullValue()));
                assertThat(result.getSecond(), is(notNullValue()));
            } else {
                assertThat(result.getFirst(), is(notNullValue()));
                assertThat(result.getSecond(), is(notNullValue()));
            }
        }
    }

    @Test
    public void findAllKppOktmoPairsByFilterTest() {
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByFilter(ReportFormCreationKppOktmoPairFilter.builder()
                        .reportPeriodId(2).departmentId(2).relevanceDate(new LocalDate(2018, 1, 1).toDate()).build(),
                PagingParams.getInstance(1, 10));
        assertThat(kppOktmoPairs, containsInAnyOrder(
                new ReportFormCreationKppOktmoPair("000000002", "111", "действует до 31.12.2017"),
                new ReportFormCreationKppOktmoPair("000000003", "111", null),
                new ReportFormCreationKppOktmoPair("000000004", "111", null),
                new ReportFormCreationKppOktmoPair("000000005", "111", null)));
    }

    @Test
    public void findAllKppOktmoPairsByFilterTest2() {
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByFilter(ReportFormCreationKppOktmoPairFilter.builder()
                        .declarationId(1L).reportPeriodId(2).departmentId(2).relevanceDate(new LocalDate(2018, 1, 1).toDate()).build(),
                PagingParams.getInstance(1, 10));
        assertThat(kppOktmoPairs, containsInAnyOrder(
                new ReportFormCreationKppOktmoPair("000000001", "111", "не относится к ТБ в периоде"),
                new ReportFormCreationKppOktmoPair("000000002", "111", "действует до 31.12.2017"),
                new ReportFormCreationKppOktmoPair("000000003", "111", null),
                new ReportFormCreationKppOktmoPair("000000004", "111", null),
                new ReportFormCreationKppOktmoPair("000000006", "111", "не относится к ТБ в периоде")));
    }

    @Test
    public void findAllKppOktmoPairsByFilterTestPaging() {
        PagingParams paging = PagingParams.getInstance(2, 2);
        paging.setProperty("kpp desc, oktmo");
        paging.setDirection("desc");
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByFilter(ReportFormCreationKppOktmoPairFilter.builder()
                        .name("000").reportPeriodId(2).departmentId(2).relevanceDate(new LocalDate(2018, 1, 1).toDate()).build(),
                paging);
        assertThat(kppOktmoPairs, contains(
                new ReportFormCreationKppOktmoPair("000000003", "111", null),
                new ReportFormCreationKppOktmoPair("000000002", "111", "действует до 31.12.2017")));
    }

    @Test
    public void findAllKppOktmoPairsByFilterTestPaging2() {
        PagingParams paging = PagingParams.getInstance(2, 2);
        paging.setProperty("kpp desc, oktmo");
        paging.setDirection("desc");
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByFilter(ReportFormCreationKppOktmoPairFilter.builder()
                        .name("000").declarationId(1L).reportPeriodId(2).departmentId(2).relevanceDate(new LocalDate(2018, 1, 1).toDate()).build(),
                paging);
        assertThat(kppOktmoPairs, contains(
                new ReportFormCreationKppOktmoPair("000000003", "111", null),
                new ReportFormCreationKppOktmoPair("000000002", "111", "действует до 31.12.2017")));
    }

    @Test
    public void existsByKppAndOkmtoAndPeriodId() {
        assertTrue(departmentConfigDao.existsByKppAndOkmtoAndPeriodId("000000001", "111", 1));
        assertFalse(departmentConfigDao.existsByKppAndOkmtoAndPeriodId("000000001", "111", 2));
        assertTrue(departmentConfigDao.existsByKppAndOkmtoAndPeriodId("000000003", "111", 2));
    }

    private DeclarationData declarationData(long id, int reportPeriodId, int departmentId) {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(id);
        declarationData.setReportPeriodId(reportPeriodId);
        declarationData.setDepartmentId(departmentId);
        return declarationData;
    }
}
