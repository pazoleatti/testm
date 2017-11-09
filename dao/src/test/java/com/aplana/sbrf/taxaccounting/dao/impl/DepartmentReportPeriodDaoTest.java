package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.LocalDateTime;
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
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(-1);
        Assert.assertNull(departmentReportPeriod);
        // 2
        departmentReportPeriod = departmentReportPeriodDao.get(303);
        Assert.assertNotNull(departmentReportPeriod);
        Assert.assertEquals(1, departmentReportPeriod.getDepartmentId().intValue());
        Assert.assertEquals(20, departmentReportPeriod.getReportPeriod().getId().intValue());
        Assert.assertTrue(departmentReportPeriod.isActive());
        Assert.assertEquals(SIMPLE_DATE_FORMAT.parse("02.01.2014"), departmentReportPeriod.getCorrectionDate().toDate());
        // 3
        departmentReportPeriod = departmentReportPeriodDao.get(101);
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
                departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
        Assert.assertEquals(37, departmentReportPeriodList.size());
        // Фильтр по подразделениям
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(4));
        departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            Assert.assertEquals(4, departmentReportPeriod.getDepartmentId().intValue());
        }
        // Фильтр по отчетным периодам
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setReportPeriodIdList(Arrays.asList(1, 2));
        departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            long reportPeriodId = departmentReportPeriod.getReportPeriod().getId();
            Assert.assertTrue(reportPeriodId == 1 || reportPeriodId == 2);
        }
        // Фильтр по виду налога
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setTaxTypeList(Arrays.asList(TaxType.NDFL));
        departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            Assert.assertEquals(TaxType.NDFL, departmentReportPeriod.getReportPeriod().getTaxPeriod().getTaxType());
        }
        // Фильтр по открытости периода
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setIsActive(false);
        departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            Assert.assertFalse(departmentReportPeriod.isActive());
        }
        // Фильтр по корректирующему периоду
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setIsCorrection(true);
        departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            Assert.assertNotNull(departmentReportPeriod.getCorrectionDate());
        }
        // Фильтр по дате корректирющего периода 1
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setCorrectionDate(new LocalDateTime(SIMPLE_DATE_FORMAT.parse("02.01.2014")));
        departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            Assert.assertEquals(SIMPLE_DATE_FORMAT.parse("02.01.2014"), departmentReportPeriod.getCorrectionDate().toDate());
        }
        // Фильтр по дате корректирющего периода 2
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setCorrectionDate(new LocalDateTime(SIMPLE_DATE_FORMAT.parse("02.01.2014")));
        departmentReportPeriodFilter.setIsCorrection(false);
        departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
        Assert.assertTrue(departmentReportPeriodList.isEmpty());
        // Множественный фильтр
        departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(1, 2));
        departmentReportPeriodFilter.setIsActive(true);
        departmentReportPeriodFilter.setIsCorrection(true);
        departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
        Assert.assertEquals(2, departmentReportPeriodList.size());
        Assert.assertEquals(303, departmentReportPeriodList.get(0).getId().intValue());
        Assert.assertEquals(307, departmentReportPeriodList.get(1).getId().intValue());
    }

    @Test
    public void getListIdsByFilterTest() throws ParseException {
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        // Пустой фильтр
        List<Long> departmentReportPeriodList =
                departmentReportPeriodDao.getListIdsByFilter(departmentReportPeriodFilter);
        Assert.assertEquals(37, departmentReportPeriodList.size());
    }

    @Test
    public void saveTest() {
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        LocalDateTime date = new LocalDateTime();
        departmentReportPeriod.setActive(true);
        departmentReportPeriod.setCorrectionDate(date);
        departmentReportPeriod.setDepartmentId(1);
        departmentReportPeriod.setReportPeriod(reportPeriodDao.get(1));
        departmentReportPeriod = departmentReportPeriodDao.save(departmentReportPeriod);
        DepartmentReportPeriod savedDepartmentReportPeriod = departmentReportPeriodDao.findOne(departmentReportPeriod.getId());
        Assert.assertEquals(DateUtils.truncate(date.toDate(), Calendar.DATE), DateUtils.truncate(savedDepartmentReportPeriod.getCorrectionDate().toDate(), Calendar.DATE));
        Assert.assertEquals(departmentReportPeriod.getId(), savedDepartmentReportPeriod.getId());
        Assert.assertTrue(savedDepartmentReportPeriod.isActive());
        Assert.assertEquals(1, savedDepartmentReportPeriod.getDepartmentId().intValue());
        Assert.assertEquals(1, savedDepartmentReportPeriod.getReportPeriod().getId().intValue());
    }

    @Test
    public void saveBatchTest() {
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = reportPeriodDao.get(11);
        departmentReportPeriod.setActive(true);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        List<Integer> depIds = new ArrayList<Integer>();
        depIds.add(2);
        depIds.add(3);
        depIds.add(4);
        departmentReportPeriodDao.save(departmentReportPeriod, depIds);

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setIsActive(true);
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriod.getId()));
        filter.setDepartmentIdList(depIds);
        List<DepartmentReportPeriod> savedDepartmentReportPeriods = departmentReportPeriodDao.getListByFilter(filter);
        Assert.assertEquals(savedDepartmentReportPeriods.size(), 3);

    }

    @Test
    public void updateActiveTest() {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(101);
        Assert.assertTrue(departmentReportPeriod.isActive());
        departmentReportPeriodDao.updateActive(101, false);
        departmentReportPeriod = departmentReportPeriodDao.get(101);
        Assert.assertFalse(departmentReportPeriod.isActive());
    }

    @Test
    public void batchUpdateActiveTest1() {
        departmentReportPeriodDao.updateActive(Arrays.asList(101L, 201L, 401L), 1, true);
        Assert.assertTrue(departmentReportPeriodDao.get(101).isActive());
    }

    @Test
    public void batchUpdateActiveTest2() {
        departmentReportPeriodDao.updateActive(Arrays.asList(101L, 201L, 401L), 1, true);
        Assert.assertTrue(departmentReportPeriodDao.get(101).isActive());
    }

    @Test
    public void updateCorrectionDateTest() {
        departmentReportPeriodDao.updateCorrectionDate(101L, null);
        Assert.assertNull(departmentReportPeriodDao.get(101).getCorrectionDate());
        LocalDateTime date = new LocalDateTime();
        departmentReportPeriodDao.updateCorrectionDate(101L, date);
        Assert.assertEquals(DateUtils.truncate(date.toDate(), Calendar.DATE), DateUtils.truncate(departmentReportPeriodDao.get(101).getCorrectionDate().toDate(), Calendar.DATE));
    }

    //@Test
    public void getCorrectionNumberTest() {
        Assert.assertNull(departmentReportPeriodDao.getCorrectionNumber(-1));
        Assert.assertEquals(0, departmentReportPeriodDao.getCorrectionNumber(101).intValue());
        Assert.assertEquals(0, departmentReportPeriodDao.getCorrectionNumber(301).intValue());
        Assert.assertEquals(1, departmentReportPeriodDao.getCorrectionNumber(302).intValue());
        Assert.assertEquals(2, departmentReportPeriodDao.getCorrectionNumber(303).intValue());
    }

    @Test
    public void getFirstTest() {
        Assert.assertEquals(301, departmentReportPeriodDao.getFirst(1, 20).getId().intValue());
        Assert.assertEquals(601, departmentReportPeriodDao.getFirst(6, 1).getId().intValue());
        Assert.assertEquals(101, departmentReportPeriodDao.getFirst(1, 1).getId().intValue());
        Assert.assertNull(departmentReportPeriodDao.getLast(-1, -1));
    }

    @Test
    public void getLastTest() {
        Assert.assertEquals(303, departmentReportPeriodDao.getLast(1, 20).getId().intValue());
        Assert.assertEquals(603, departmentReportPeriodDao.getLast(6, 1).getId().intValue());
        Assert.assertEquals(101, departmentReportPeriodDao.getLast(1, 1).getId().intValue());
        Assert.assertNull(departmentReportPeriodDao.getLast(-1, -1));
    }

    @Test
    public void existLargeCorrectionTest() throws ParseException {
        Assert.assertTrue(departmentReportPeriodDao.existLargeCorrection(1, 20, new LocalDateTime(SIMPLE_DATE_FORMAT.parse("01.01.2011"))));
    }

    @Test
    public void deleteTest1() {
        departmentReportPeriodDao.delete(102);
        Assert.assertNull(departmentReportPeriodDao.get(102));
    }

    @Test
    public void deleteTest2() {
        departmentReportPeriodDao.delete(Arrays.asList(102L));
        Assert.assertNull(departmentReportPeriodDao.get(102));
    }

    @Test
    public void getCorrectionDateListByReportPeriodTest() throws ParseException {
        // Не задан параметр — пустая мапа
        Assert.assertNotNull(departmentReportPeriodDao.getCorrectionDateListByReportPeriod(null));

        // Отчетный период с id = 1
        int reportPeriodId = 1;
        List<Integer> reportPeriodIdList = Arrays.asList(reportPeriodId);
        Map<Integer, List<Date>> map = departmentReportPeriodDao.getCorrectionDateListByReportPeriod(reportPeriodIdList);
        Assert.assertEquals(reportPeriodIdList.size(), map.size());
        List<Date> dateList = map.get(reportPeriodId);
        Assert.assertEquals(2, dateList.size());
        Assert.assertTrue(dateList.contains(SIMPLE_DATE_FORMAT.parse("01.01.2014")));
        Assert.assertTrue(dateList.contains(SIMPLE_DATE_FORMAT.parse("01.02.2014")));

        // Отчетные периоды с id = 2, 20, 22
        reportPeriodIdList = Arrays.asList(2, 20, 22);
        map = departmentReportPeriodDao.getCorrectionDateListByReportPeriod(reportPeriodIdList);
        Assert.assertEquals(2, map.size());
        Assert.assertNull(map.get(2));
        dateList = map.get(20);
        Assert.assertEquals(2, dateList.size());
        Assert.assertTrue(dateList.contains(SIMPLE_DATE_FORMAT.parse("01.01.2014")));
        Assert.assertTrue(dateList.contains(SIMPLE_DATE_FORMAT.parse("02.01.2014")));
        dateList = map.get(22);
        Assert.assertEquals(2, dateList.size());
        Assert.assertTrue(dateList.contains(SIMPLE_DATE_FORMAT.parse("01.01.2014")));
        Assert.assertTrue(dateList.contains(SIMPLE_DATE_FORMAT.parse("02.01.2014")));
    }
}
