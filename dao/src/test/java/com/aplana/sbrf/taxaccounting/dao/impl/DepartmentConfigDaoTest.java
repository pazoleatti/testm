package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationSourceDataSearchFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentConfigDaoTest.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DepartmentConfigDaoTest {

    @Autowired
    private DepartmentConfigDao departmentConfigDao;

    @Test
    public void findByIdTest() {
        assertNotNull(departmentConfigDao.findById(10L));
    }

    @Test
    public void findAllByDepartmentIdTest() {
        assertThat(departmentConfigDao.findAllByDepartmentId(3), hasSize(3));
    }

    @Test
    public void findAllByFilterTest1() {
        List<DepartmentConfig> departmentConfigs = departmentConfigDao.findPageByFilter(DepartmentConfigsFilter.builder()
                        .departmentId(2).build(),
                PagingParams.getInstance(1, 10, "kpp", "desc"));
        assertThat(departmentConfigs, hasSize(9));

        Set<String> kpps = new LinkedHashSet<>();
        for (DepartmentConfig departmentConfig : departmentConfigs) {
            kpps.add(departmentConfig.getKpp());
        }
        assertThat(kpps, contains("000000006", "000000005", "000000004", "000000003", "000000002", "000000001"));
    }

    @Test
    public void findAllByFilterTest2() {
        List<DepartmentConfig> departmentConfigs = departmentConfigDao.findPageByFilter(DepartmentConfigsFilter.builder()
                        .departmentId(2).kpp("03").oktmo("1").taxOrganCode("05")
                        .relevanceDate(newDate(1, 1, 2019)).build(),
                PagingParams.getInstance(1, 100));
        assertThat(departmentConfigs, hasSize(1));
        assertThat(departmentConfigs.get(0).getId(), equalTo(122L));
    }

    @Test
    public void findAllByFilterTest3() {
        PagingResult<DepartmentConfig> departmentConfigPage = departmentConfigDao.findPageByFilter(DepartmentConfigsFilter.builder()
                        .departmentId(2).build(),
                PagingParams.getInstance(2, 4));
        assertThat(departmentConfigPage, hasSize(4));
        assertThat(departmentConfigPage.getTotalCount(), equalTo(9));
    }

    @Test
    public void findAllByFilterTest4() {
        List<DepartmentConfig> departmentConfigs = departmentConfigDao.findPageByFilter(DepartmentConfigsFilter.builder().build(),
                PagingParams.getInstance(1, 100));
        assertThat(departmentConfigs, hasSize(15));
    }

    @Test
    public void countByFilterTest() {
        int count = departmentConfigDao.countByFilter(DepartmentConfigsFilter.builder()
                .departmentId(2).build());
        assertThat(count, equalTo(9));
    }

    @Test
    public void findPrevTest() {
        DepartmentConfig departmentConfig = departmentConfigDao.findPrevById(122);
        assertThat(departmentConfig.getId(), is(12L));
    }

    @Test
    public void findNextTest() {
        DepartmentConfig departmentConfig = departmentConfigDao.findNextById(12);
        assertThat(departmentConfig.getId(), is(122L));
    }

    @Test
    public void findKppOktmoPairsTest() {
        List<Pair<String, String>> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByDepartmentIdIn(asList(2, 3), newDate(1, 1, 2017));
        assertThat(kppOktmoPairs.size(), is(7));
        assertThat(kppOktmoPairs, is(Matchers.containsInAnyOrder(
                new Pair("000000002", "111"), new Pair("000000003", "111"), new Pair("000000004", "111"), new Pair("000000005", "111"),
                new Pair("000000006", "111"), new Pair("000000010", "222"), new Pair("000000011", "222")
        )));
    }

    @Test
    public void findAllKppByFilterTest() {
        PagingResult<KppSelect> kppSelectList = departmentConfigDao.findAllKppByDepartmentIdAndKppContaining(2, null, PagingParams.getInstance(1, 10));
        assertThat(kppSelectList.size(), is(6));

        kppSelectList = departmentConfigDao.findAllKppByDepartmentIdAndKppContaining(2, null, PagingParams.getInstance(2, 2));
        assertThat(kppSelectList.size(), is(2));

        kppSelectList = departmentConfigDao.findAllKppByDepartmentIdAndKppContaining(3, "01", PagingParams.getInstance(1, 10));
        assertThat(kppSelectList.size(), is(2));
    }

    @Test
    public void findAllByDeclarationTest() {
        List<Pair<KppOktmoPair, DepartmentConfig>> results = departmentConfigDao.findAllByDeclaration(
                declarationData(1, 2, 2), newDate(1, 1, 2018));
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
    public void findAllKppOKtmoPairsByDeclaration1() {
        assertThat(departmentConfigDao.findAllKppOKtmoPairsByDeclaration(1, 2, 2, newDate(1, 1, 2018)), hasSize(3));
    }

    @Test
    public void findAllKppOKtmoPairsByDeclaration2() {
        assertThat(departmentConfigDao.findAllKppOKtmoPairsByDeclaration(1, null, 2, newDate(1, 1, 2018)), hasSize(4));
    }

    @Test
    public void test_indAllKppOktmoPairs1() {
        assertThat(departmentConfigDao.findAllKppOKtmoPairsByDeclaration(1, null, 2, newDate(1, 1, 2018)), hasSize(4));
    }

    @Test
    public void test_indAllKppOktmoPairs2() {
        assertThat(departmentConfigDao.findAllKppOKtmoPairsByDeclaration(1, 2, 2, newDate(1, 1, 2018)), hasSize(3));
    }

    @Test
    public void findAllKppOktmoPairsByPnf() {
        assertThat(departmentConfigDao.findAllKppOktmoPairsByFilter(new KppOktmoPairFilter()
                .reportPeriodId(2).departmentId(2).relevanceDate(newDate(1, 1, 2018)), null), hasSize(4));
    }

    @Test
    public void findAllKppOktmoPairsByKnf() {
        assertThat(departmentConfigDao.findAllKppOktmoPairsByFilter(new KppOktmoPairFilter()
                .reportPeriodId(2).relevanceDate(newDate(1, 1, 2018)), null), hasSize(8));
    }

    @Test
    public void findAllKppOktmoPairsByFilterTest() {
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByFilter(new KppOktmoPairFilter()
                        .reportPeriodId(2).departmentId(2).relevanceDate(newDate(1, 1, 2018)),
                PagingParams.getInstance(1, 10));
        assertThat(kppOktmoPairs, containsInAnyOrder(
                new ReportFormCreationKppOktmoPair("000000002", "111", "действует до 31.12.2017"),
                new ReportFormCreationKppOktmoPair("000000003", "111", null),
                new ReportFormCreationKppOktmoPair("000000004", "111", null),
                new ReportFormCreationKppOktmoPair("000000005", "111", null)));
    }

    @Test
    public void findAllKppOktmoPairsByFilterTest2() {
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByFilter(new KppOktmoPairFilter()
                        .declarationId(1L).reportPeriodId(2).departmentId(2).relevanceDate(newDate(1, 1, 2018)),
                PagingParams.getInstance(1, 10));
        assertThat(kppOktmoPairs, containsInAnyOrder(
                new ReportFormCreationKppOktmoPair("000000001", "111", "не относится к ТБ в периоде"),
                new ReportFormCreationKppOktmoPair("000000002", "111", "действует до 31.12.2017"),
                new ReportFormCreationKppOktmoPair("000000003", "111", null),
                new ReportFormCreationKppOktmoPair("000000004", "111", null),
                new ReportFormCreationKppOktmoPair("000000006", "111", "не относится к ТБ в периоде")));
    }

    @Test
    public void findAllKppOktmoPairsByFilterTest3() {
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByFilter(new KppOktmoPairFilter()
                        .departmentId(2).relevanceDate(newDate(31, 12, 2017)),
                PagingParams.getInstance(1, 10));
        assertThat(kppOktmoPairs, containsInAnyOrder(
                new ReportFormCreationKppOktmoPair("000000002", "111", "действует до 31.12.2017"),
                new ReportFormCreationKppOktmoPair("000000003", "111", null),
                new ReportFormCreationKppOktmoPair("000000006", "111", "действует до 31.12.2017"),
                new ReportFormCreationKppOktmoPair("000000004", "111", null),
                new ReportFormCreationKppOktmoPair("000000005", "111", null)));
    }

    @Test
    public void findAllKppOktmoPairsByFilterTestPaging() {
        PagingParams paging = PagingParams.getInstance(2, 2);
        paging.setProperty("kpp desc, oktmo");
        paging.setDirection("desc");
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByFilter(new KppOktmoPairFilter()
                        .name("000").reportPeriodId(2).departmentId(2).relevanceDate(newDate(1, 1, 2018)),
                paging);
        assertThat(kppOktmoPairs, contains(
                new ReportFormCreationKppOktmoPair("000000003", "111", null),
                new ReportFormCreationKppOktmoPair("000000002", "111", "действует до 31.12.2017")));
    }

    @Test
    public void findAllKppOktmoPairsByFilterTestPaging2() {
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = departmentConfigDao.findAllKppOktmoPairsByFilter(new KppOktmoPairFilter()
                        .name("000").declarationId(1L).reportPeriodId(2).departmentId(2).relevanceDate(newDate(1, 1, 2018)),
                PagingParams.getInstance(2, 2, "kpp desc, oktmo", "desc"));
        assertThat(kppOktmoPairs, contains(
                new ReportFormCreationKppOktmoPair("000000003", "111", null),
                new ReportFormCreationKppOktmoPair("000000002", "111", "действует до 31.12.2017")));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createTest() {
        DepartmentConfig departmentConfig = new DepartmentConfig()
                .kpp("1").oktmo(new RefBookOktmo().id(1L))
                .startDate(newDate(1, 1, 2016))
                .endDate(newDate(31, 12, 2017))
                .department(new RefBookDepartment().id(2))
                .taxOrganCode("1234")
                .presentPlace(new RefBookPresentPlace().id(1L))
                .signatoryMark(new RefBookSignatoryMark().id(1L));
        departmentConfigDao.create(departmentConfig);
        assertNotNull(departmentConfigDao.findById(departmentConfig.getId()));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateTest() {
        DepartmentConfig departmentConfig = departmentConfigDao.findById(10L);
        departmentConfig.setApproveDocName("approveDocName");
        departmentConfigDao.update(departmentConfig);
        departmentConfig = departmentConfigDao.findById(10L);
        assertThat(departmentConfig.getApproveDocName(), equalTo("approveDocName"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void deleteByIdTest() {
        int countBefore = departmentConfigDao.countByFilter(new DepartmentConfigsFilter());
        departmentConfigDao.deleteById(10L);
        int countAfter = departmentConfigDao.countByFilter(new DepartmentConfigsFilter());
        assertThat(countAfter, equalTo(countBefore - 1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void deleteByDepartmentIdTest() {
        List<DepartmentConfig> before = departmentConfigDao.findAllByDepartmentId(3);
        departmentConfigDao.deleteByDepartmentId(3);
        List<DepartmentConfig> after = departmentConfigDao.findAllByDepartmentId(3);
        assertThat(after.size(), equalTo(before.size() - 3));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateStartDate() {
        assertThat(departmentConfigDao.findById(10).getStartDate(), equalTo(newDate(1, 1, 2016)));
        departmentConfigDao.updateStartDate(10, newDate(3, 3, 2016));
        assertThat(departmentConfigDao.findById(10).getStartDate(), equalTo(newDate(3, 3, 2016)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateEndDate() {
        assertThat(departmentConfigDao.findById(10).getEndDate(), equalTo(newDate(31, 12, 2016)));
        departmentConfigDao.updateEndDate(10, newDate(3, 3, 2016));
        assertThat(departmentConfigDao.findById(10).getEndDate(), equalTo(newDate(3, 3, 2016)));
    }

    @Test
    public void findDepartmentConfigsForAutoCorrectKppAndOktmoWithOnlyOneActualConfig() {
        Date periodEndDate = newDate(31, 3, 2019);
        ConsolidationSourceDataSearchFilter filter = ConsolidationSourceDataSearchFilter.builder()
                .periodStartDate(newDate(1,3, 2019))
                .periodEndDate(periodEndDate)
                .build();

        List<DepartmentConfig> allByKppAndOktmoAndFilter =
                departmentConfigDao.findAllByKppAndOktmoAndFilter("000000013", "222", filter);

        assertThat(allByKppAndOktmoAndFilter.size(), equalTo(1));
        assertThat(allByKppAndOktmoAndFilter, contains(
                DepartmentConfig.builder().id(20L).build()
        ));
        assertThat(allByKppAndOktmoAndFilter.get(0).getEndDate(), anyOf(lessThan(periodEndDate), nullValue()));
    }

    @Test
    public void findDepartmentConfigsForAutoCorrectWithNonOverlappingPeriodAndDepartmentConfig() {
        ConsolidationSourceDataSearchFilter filter = ConsolidationSourceDataSearchFilter.builder()
                .periodStartDate(newDate(1,12, 2018))
                .periodEndDate(newDate(31, 12, 2018))
                .build();

        List<DepartmentConfig> allByKppAndOktmoAndFilter =
                departmentConfigDao.findAllByKppAndOktmoAndFilter("000000013", "222", filter);

        assertThat(allByKppAndOktmoAndFilter.size(), equalTo(0));
    }

    private Date newDate(int day, int month, int year) {
        return new LocalDate(year, month, day).toDate();
    }

    private DeclarationData declarationData(long id, int reportPeriodId, int departmentId) {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(id);
        declarationData.setReportPeriodId(reportPeriodId);
        declarationData.setDepartmentId(departmentId);
        return declarationData;
    }
}
