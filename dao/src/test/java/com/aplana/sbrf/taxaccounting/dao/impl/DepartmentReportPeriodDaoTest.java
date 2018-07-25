package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
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
import java.util.*;

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
        Assert.assertEquals(1, departmentReportPeriod.getDepartmentId().intValue());
        Assert.assertEquals(20, departmentReportPeriod.getReportPeriod().getId().intValue());
        Assert.assertTrue(departmentReportPeriod.isActive());
        Assert.assertEquals(SIMPLE_DATE_FORMAT.parse("02.01.2014"), departmentReportPeriod.getCorrectionDate());
        // 3
        departmentReportPeriod = departmentReportPeriodDao.fetchOne(101);
        Assert.assertNotNull(departmentReportPeriod);
        Assert.assertEquals(1, departmentReportPeriod.getDepartmentId().intValue());
        Assert.assertEquals(1, departmentReportPeriod.getReportPeriod().getId().intValue());
        Assert.assertTrue(departmentReportPeriod.isActive());
        Assert.assertNull(departmentReportPeriod.getCorrectionDate());
    }

    @Test
    public void getListByFilterTest() throws ParseException {
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        // Пустой фильтр
        List<DepartmentReportPeriod> departmentReportPeriodList =
                departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        Assert.assertEquals(37, departmentReportPeriodList.size());
        // Фильтр по подразделениям
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(4));
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            Assert.assertEquals(4, departmentReportPeriod.getDepartmentId().intValue());
        }
        // Фильтр по отчетным периодам
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setReportPeriodIdList(Arrays.asList(1, 2));
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
            Assert.assertFalse(departmentReportPeriod.isActive());
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
            Assert.assertEquals(SIMPLE_DATE_FORMAT.parse("02.01.2014"), departmentReportPeriod.getCorrectionDate());
        }
        // Фильтр по дате корректирющего периода 2
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setCorrectionDate(SIMPLE_DATE_FORMAT.parse("02.01.2014"));
        departmentReportPeriodFilter.setIsCorrection(false);
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        Assert.assertTrue(departmentReportPeriodList.isEmpty());
        // Множественный фильтр
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(1, 2));
        departmentReportPeriodFilter.setIsActive(true);
        departmentReportPeriodFilter.setIsCorrection(true);
        departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        Assert.assertEquals(2, departmentReportPeriodList.size());
        Assert.assertEquals(303, departmentReportPeriodList.get(0).getId().intValue());
        Assert.assertEquals(307, departmentReportPeriodList.get(1).getId().intValue());
    }

    @Test
    public void getListIdsByFilterTest() throws ParseException {
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        // Пустой фильтр
        List<Integer> departmentReportPeriodList =
                departmentReportPeriodDao.fetchAllIdsByFilter(departmentReportPeriodFilter);
        Assert.assertEquals(37, departmentReportPeriodList.size());
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


        Assert.assertEquals(DateUtils.truncate(date, Calendar.DATE), DateUtils.truncate(savedDepartmentReportPeriod.getCorrectionDate(), Calendar.DATE));
        Assert.assertTrue(savedDepartmentReportPeriod.isActive());
        Assert.assertEquals(1, savedDepartmentReportPeriod.getDepartmentId().intValue());
        Assert.assertEquals(1, savedDepartmentReportPeriod.getReportPeriod().getId().intValue());
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
        for (Integer departmentId : depIds) {
            departmentReportPeriod.setDepartmentId(departmentId);
            departmentReportPeriodDao.create(departmentReportPeriod);
        }

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setIsActive(true);
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriod.getId()));
        filter.setDepartmentIdList(depIds);
        List<DepartmentReportPeriod> savedDepartmentReportPeriods = departmentReportPeriodDao.fetchAllByFilter(filter);
        Assert.assertEquals(savedDepartmentReportPeriods.size(), 3);

    }

    @Test
    public void updateActiveTest() {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(101);
        Assert.assertTrue(departmentReportPeriod.isActive());
        departmentReportPeriodDao.updateActive(101, false);
        departmentReportPeriod = departmentReportPeriodDao.fetchOne(101);
        Assert.assertFalse(departmentReportPeriod.isActive());
    }

    @Test
    public void batchUpdateActiveTest1() {
        departmentReportPeriodDao.updateActive(Arrays.asList(101, 201, 401), 1, true);
        Assert.assertTrue(departmentReportPeriodDao.fetchOne(101).isActive());
    }

    @Test
    public void batchUpdateActiveTest2() {
        departmentReportPeriodDao.updateActive(Arrays.asList(101, 201, 401), 1, true);
        Assert.assertTrue(departmentReportPeriodDao.fetchOne(101).isActive());
    }

    @Test
    public void getFirstTest() {
        Assert.assertEquals(301, departmentReportPeriodDao.fetchFirst(1, 20).getId().intValue());
        Assert.assertEquals(601, departmentReportPeriodDao.fetchFirst(6, 1).getId().intValue());
        Assert.assertEquals(101, departmentReportPeriodDao.fetchFirst(1, 1).getId().intValue());
        Assert.assertNull(departmentReportPeriodDao.fetchLast(-1, -1));
    }

    @Test
    public void getLastTest() {
        Assert.assertEquals(303, departmentReportPeriodDao.fetchLast(1, 20).getId().intValue());
        Assert.assertEquals(603, departmentReportPeriodDao.fetchLast(6, 1).getId().intValue());
        Assert.assertEquals(101, departmentReportPeriodDao.fetchLast(1, 1).getId().intValue());
        Assert.assertNull(departmentReportPeriodDao.fetchLast(-1, -1));
    }

    @Test
    public void existLargeCorrectionTest() throws ParseException {
        Assert.assertTrue(departmentReportPeriodDao.checkExistLargeCorrection(1, 20, SIMPLE_DATE_FORMAT.parse("01.01.2011")));
    }

    @Test
    public void deleteTest2() {
        departmentReportPeriodDao.delete(Arrays.asList(102));
        Assert.assertNull(departmentReportPeriodDao.fetchOne(102));
    }

    @Test
    public void findAllTest() {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setYearStart(1900);
        filter.setYearEnd(2099);

        //1
        filter.setDepartmentId(1);
        Assert.assertNotEquals(0, departmentReportPeriodDao.fetchJournalItemByFilter(filter).size());

        //2
        filter.setDepartmentId(-1);
        Assert.assertEquals(0, departmentReportPeriodDao.fetchJournalItemByFilter(filter).size());

        //3 с пустым фильтром
        Assert.assertNotEquals(0, departmentReportPeriodDao.fetchJournalItemByFilter(new DepartmentReportPeriodFilter()).size());
    }

}
