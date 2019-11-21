package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.builder.DepartmentReportPeriodBuilder;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentReportPeriodDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DepartmentReportPeriodDaoTest {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Test
    public void getTest() throws ParseException {
        // 1
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(-1);
        Assert.assertNull(departmentReportPeriod);
        // 2
        departmentReportPeriod = departmentReportPeriodDao.fetchOne(303);
        Assert.assertNotNull(departmentReportPeriod);
        assertEquals(1, departmentReportPeriod.getDepartmentId().intValue());
        assertEquals(20, departmentReportPeriod.getReportPeriod().getId().intValue());
        Assert.assertTrue(departmentReportPeriod.isActive());
        assertEquals(SIMPLE_DATE_FORMAT.parse("02.01.2014"), departmentReportPeriod.getCorrectionDate());
        // 3
        departmentReportPeriod = departmentReportPeriodDao.fetchOne(101);
        Assert.assertNotNull(departmentReportPeriod);
        assertEquals(1, departmentReportPeriod.getDepartmentId().intValue());
        assertEquals(1, departmentReportPeriod.getReportPeriod().getId().intValue());
        Assert.assertTrue(departmentReportPeriod.isActive());
        Assert.assertNull(departmentReportPeriod.getCorrectionDate());
    }

    @Test
    public void getListByFilterTest() throws ParseException {
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        // Пустой фильтр
        List<DepartmentReportPeriod> departmentReportPeriodList =
                departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        assertEquals(51, departmentReportPeriodList.size());
        // Фильтр по подразделениям
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(asList(4));
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            assertEquals(4, departmentReportPeriod.getDepartmentId().intValue());
        }
        // Фильтр по отчетным периодам
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setReportPeriodIdList(asList(1, 2));
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            int reportPeriodId = departmentReportPeriod.getReportPeriod().getId();
            Assert.assertTrue(reportPeriodId == 1 || reportPeriodId == 2);
        }

        // Фильтр по открытости периода
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setIsActive(false);
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            assertFalse(departmentReportPeriod.isActive());
        }
        // Фильтр по корректирующему периоду
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setIsCorrection(true);
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            Assert.assertNotNull(departmentReportPeriod.getCorrectionDate());
        }
        // Фильтр по дате корректирющего периода 1
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setCorrectionDate(SIMPLE_DATE_FORMAT.parse("02.01.2014"));
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            assertEquals(SIMPLE_DATE_FORMAT.parse("02.01.2014"), departmentReportPeriod.getCorrectionDate());
        }
        // Фильтр по дате корректирющего периода 2
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setCorrectionDate(SIMPLE_DATE_FORMAT.parse("02.01.2014"));
        departmentReportPeriodFilter.setIsCorrection(false);
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        Assert.assertTrue(departmentReportPeriodList.isEmpty());
        // Множественный фильтр
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(asList(1, 2));
        departmentReportPeriodFilter.setIsActive(true);
        departmentReportPeriodFilter.setIsCorrection(true);
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        assertEquals(7, departmentReportPeriodList.size());
        assertEquals(303, departmentReportPeriodList.get(0).getId().intValue());
        assertEquals(307, departmentReportPeriodList.get(1).getId().intValue());
    }

    @Test
    public void getListIdsByFilterTest() throws ParseException {
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        // Пустой фильтр
        List<Integer> departmentReportPeriodList =
                departmentReportPeriodDao.fetchAllIdsByFilter(departmentReportPeriodFilter);
        assertEquals(51, departmentReportPeriodList.size());
    }

    @Test
    public void saveTest() {
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        Date date = new Date();
        departmentReportPeriod.setIsActive(true);
        departmentReportPeriod.setCorrectionDate(date);
        departmentReportPeriod.setDepartmentId(1);
        departmentReportPeriod.setReportPeriod(reportPeriodDao.fetchOne(1));
        departmentReportPeriodDao.create(departmentReportPeriod);

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setIsActive(true);
        filter.setIsCorrection(true);
        filter.setDepartmentId(1);
        filter.setReportPeriod(reportPeriodDao.fetchOne(1));
        List<DepartmentReportPeriod> savedDepartmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(filter);
        DepartmentReportPeriod savedDepartmentReportPeriod = savedDepartmentReportPeriodList.get(0);


        assertEquals(DateUtils.truncate(date, Calendar.DATE), DateUtils.truncate(savedDepartmentReportPeriod.getCorrectionDate(), Calendar.DATE));
        Assert.assertTrue(savedDepartmentReportPeriod.isActive());
        assertEquals(1, savedDepartmentReportPeriod.getDepartmentId().intValue());
        assertEquals(1, savedDepartmentReportPeriod.getReportPeriod().getId().intValue());
    }

    @Test
    public void saveBatchTest() {
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = reportPeriodDao.fetchOne(11);
        departmentReportPeriod.setIsActive(true);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        List<Integer> depIds = new ArrayList<Integer>();
        depIds.add(2);
        depIds.add(3);
        depIds.add(4);
        departmentReportPeriodDao.create(departmentReportPeriod, depIds);

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setIsActive(true);
        filter.setReportPeriodIdList(singletonList(reportPeriod.getId()));
        filter.setDepartmentIdList(depIds);
        List<DepartmentReportPeriod> savedDepartmentReportPeriods = departmentReportPeriodDao.fetchAllByFilter(filter);
        assertEquals(savedDepartmentReportPeriods.size(), 3);
    }

    @Test
    public void saveBatchTest2() {
        departmentReportPeriodDao.create(new DepartmentReportPeriodBuilder().department(5).reportPeriodId(11).active(false).build());
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setReportPeriodIdList(asList(11, 12, 13));
        filter.setDepartmentId(5);
        assertEquals(1, departmentReportPeriodDao.fetchAllByFilter(filter).size());

        List<DepartmentReportPeriod> departmentReportPeriods = new ArrayList<>();
        departmentReportPeriods.add(new DepartmentReportPeriodBuilder().reportPeriodId(11).active(false).build());
        departmentReportPeriods.add(new DepartmentReportPeriodBuilder().reportPeriodId(12).active(true).build());
        departmentReportPeriods.add(new DepartmentReportPeriodBuilder().reportPeriodId(13).active(true).build());

        departmentReportPeriodDao.merge(departmentReportPeriods, 5);

        assertEquals(3, departmentReportPeriodDao.fetchAllByFilter(filter).size());
    }

    @Test
    public void updateActiveTest() {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(101);
        Assert.assertTrue(departmentReportPeriod.isActive());
        departmentReportPeriodDao.updateActive(101, false);
        departmentReportPeriod = departmentReportPeriodDao.fetchOne(101);
        assertFalse(departmentReportPeriod.isActive());
    }

    @Test
    public void batchUpdateActiveTest1() {
        departmentReportPeriodDao.updateActive(asList(101, 201, 401), 1, true);
        Assert.assertTrue(departmentReportPeriodDao.fetchOne(101).isActive());
    }

    @Test
    public void batchUpdateActiveTest2() {
        departmentReportPeriodDao.updateActive(asList(101, 201, 401), 1, true);
        Assert.assertTrue(departmentReportPeriodDao.fetchOne(101).isActive());
    }

    @Test
    public void getFirstTest() {
        assertEquals(301, departmentReportPeriodDao.fetchFirst(1, 20).getId().intValue());
        assertEquals(601, departmentReportPeriodDao.fetchFirst(6, 1).getId().intValue());
        assertEquals(101, departmentReportPeriodDao.fetchFirst(1, 1).getId().intValue());
        Assert.assertNull(departmentReportPeriodDao.fetchLast(-1, -1));
    }

    @Test
    public void getLastTest() {
        assertEquals(303, departmentReportPeriodDao.fetchLast(1, 20).getId().intValue());
        assertEquals(603, departmentReportPeriodDao.fetchLast(6, 1).getId().intValue());
        assertEquals(101, departmentReportPeriodDao.fetchLast(1, 1).getId().intValue());
        Assert.assertNull(departmentReportPeriodDao.fetchLast(-1, -1));
    }

    @Test
    public void getCorrectivePeriodsSortedByFormTypePriorityTest() {
        List<DepartmentReportPeriod> departmentReportPeriods =
                departmentReportPeriodDao.getPeriodsSortedByFormTypePriority(1, "33", 2019, true);
        assertEquals(4, departmentReportPeriods.size());

        DepartmentReportPeriod departmentReportPeriod1 = departmentReportPeriods.get(0);
        assertEquals(912, (int) departmentReportPeriod1.getId());
        assertEquals(NDFL_2_2.getId(), new Long(departmentReportPeriod1.getReportPeriod().getReportPeriodTaxFormTypeId()));

        DepartmentReportPeriod departmentReportPeriod2 = departmentReportPeriods.get(1);
        assertEquals(911, (int) departmentReportPeriod2.getId());
        assertEquals(NDFL_2_1.getId(), new Long(departmentReportPeriod2.getReportPeriod().getReportPeriodTaxFormTypeId()));

        DepartmentReportPeriod departmentReportPeriod3 = departmentReportPeriods.get(2);
        assertEquals(913, (int) departmentReportPeriod3.getId());
        assertEquals(NDFL_6.getId(), new Long(departmentReportPeriod3.getReportPeriod().getReportPeriodTaxFormTypeId()));

        DepartmentReportPeriod departmentReportPeriod4 = departmentReportPeriods.get(3);
        assertEquals(914, (int) departmentReportPeriod4.getId());
        assertEquals(APPLICATION_2.getId(), new Long(departmentReportPeriod4.getReportPeriod().getReportPeriodTaxFormTypeId()));
    }

    @Test
    public void getNonCorrectivePeriodsSortedByFormTypePriorityTest() {
        List<DepartmentReportPeriod> departmentReportPeriods =
                departmentReportPeriodDao.getPeriodsSortedByFormTypePriority(1, "21", 2019, false);
        assertEquals(4, departmentReportPeriods.size());

        DepartmentReportPeriod departmentReportPeriod1 = departmentReportPeriods.get(0);
        assertEquals(902, (int) departmentReportPeriod1.getId());
        assertEquals(NDFL_2_2.getId(), new Long(departmentReportPeriod1.getReportPeriod().getReportPeriodTaxFormTypeId()));

        DepartmentReportPeriod departmentReportPeriod2 = departmentReportPeriods.get(1);
        assertEquals(901, (int) departmentReportPeriod2.getId());
        assertEquals(NDFL_2_1.getId(), new Long(departmentReportPeriod2.getReportPeriod().getReportPeriodTaxFormTypeId()));

        DepartmentReportPeriod departmentReportPeriod3 = departmentReportPeriods.get(2);
        assertEquals(903, (int) departmentReportPeriod3.getId());
        assertEquals(NDFL_6.getId(), new Long(departmentReportPeriod3.getReportPeriod().getReportPeriodTaxFormTypeId()));

        DepartmentReportPeriod departmentReportPeriod4 = departmentReportPeriods.get(3);
        assertEquals(904, (int) departmentReportPeriod4.getId());
        assertEquals(APPLICATION_2.getId(), new Long(departmentReportPeriod4.getReportPeriod().getReportPeriodTaxFormTypeId()));
    }

    @Test
    public void existLargeCorrectionTest() throws ParseException {
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriodBuilder()
                .department(1)
                .reportPeriodId(20)
                .reportPeriodTaxFormTypeId(NDFL_6.getId().intValue())
                .correctionDate(SIMPLE_DATE_FORMAT.parse("01.01.2011"))
                .build();
        Assert.assertTrue(departmentReportPeriodDao.isLaterCorrectionPeriodExists(departmentReportPeriod));
    }

    @Test
    public void deleteTest2() {
        departmentReportPeriodDao.delete(asList(102));
        Assert.assertNull(departmentReportPeriodDao.fetchOne(102));
    }

    @Test
    public void findAllTest() {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setYearStart(1900);
        filter.setYearEnd(2099);

        //1
        filter.setDepartmentId(1);
        assertNotEquals(0, departmentReportPeriodDao.fetchJournalItemByFilter(filter).size());

        //2
        filter.setDepartmentId(-1);
        assertEquals(0, departmentReportPeriodDao.fetchJournalItemByFilter(filter).size());

        //3 с пустым фильтром
        assertNotEquals(0, departmentReportPeriodDao.fetchJournalItemByFilter(new DepartmentReportPeriodFilter()).size());
    }

    @Test
    public void isExistsByReportPeriodIdAndDepartmentId() {
        assertTrue(departmentReportPeriodDao.isExistsByReportPeriodIdAndDepartmentId(1, 1));

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setReportPeriodIdList(singletonList(1));
        filter.setDepartmentId(1);
        departmentReportPeriodDao.delete(departmentReportPeriodDao.fetchAllIdsByFilter(filter));

        assertFalse(departmentReportPeriodDao.isExistsByReportPeriodIdAndDepartmentId(1, 1));
    }

    @Test
    public void isExistsByReportPeriodId() {
        assertTrue(departmentReportPeriodDao.isExistsByReportPeriodId(1));

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setReportPeriodIdList(singletonList(1));
        departmentReportPeriodDao.delete(departmentReportPeriodDao.fetchAllIdsByFilter(filter));

        assertFalse(departmentReportPeriodDao.isExistsByReportPeriodId(1));
    }
}
