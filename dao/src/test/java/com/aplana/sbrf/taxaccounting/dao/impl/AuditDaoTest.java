package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;


// TODO getLogs* не работают так как count (*) over () не поддерживается hsqldb
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"AuditDaoTest.xml"})
@Transactional(readOnly = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuditDaoTest {

    @Autowired
    private AuditDao auditDao;

    private static final int FILTER_LENGTH = 1000;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @Test
    @Transactional(readOnly = false)
    public void testAdd() {
        LogSystem logSystem = new LogSystem();
        logSystem.setId(10l);
        logSystem.setIp("192.168.72.16");
        logSystem.setEventId(3);
        logSystem.setUserLogin("controlBank");
        logSystem.setRoles("operator");
        logSystem.setFormDepartmentName("Подразделение");
        logSystem.setReportPeriodName("2013 первый квартал");
        logSystem.setDeclarationTypeName("test DeclarationType");
        logSystem.setFormTypeName("test FormType");
        logSystem.setFormKindId(2);
        logSystem.setNote("the best note");
        logSystem.setUserDepartmentName("Подразделение");
        logSystem.setFormTypeId(null);
        logSystem.setAuditFormTypeId(1);
        logSystem.setServer("server");

        auditDao.add(logSystem);
    }

    @Test
    public void testCount() {
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(new Date(1304247365000L));
        filter.setToSearchDate(new Date(1369911365000L));
        filter.setFilter("Налоговая форма");
        filter.setAuditFieldList(Arrays.asList(AuditFieldList.TYPE.getId()));

        long count = auditDao.getCount(filter);
        assertEquals(3, count);
    }

    @Test
    public void testCountForControl() throws ParseException {
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(SIMPLE_DATE_FORMAT.parse("01.05.2011"));
        filter.setToSearchDate(SIMPLE_DATE_FORMAT.parse("30.05.2013"));
        filter.setFilter("controlBank");
        filter.setAuditFieldList(Arrays.asList(AuditFieldList.ALL.getId()));

        long count = auditDao.getCountForControl(filter,
                new HashMap<AuditDao.SAMPLE_NUMBER, Collection<Integer>>() {{
                    put(AuditDao.SAMPLE_NUMBER.S_55, Arrays.asList(10));
                    put(AuditDao.SAMPLE_NUMBER.S_45, Arrays.asList(10));
                    put(AuditDao.SAMPLE_NUMBER.S_10, Arrays.asList(10));
                }});
        assertEquals(2, count);
    }

    @Test
    public void testCountForControlUnp() throws ParseException {
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(SIMPLE_DATE_FORMAT.parse("01.05.2011"));
        filter.setToSearchDate(SIMPLE_DATE_FORMAT.parse("30.05.2013"));
        filter.setFilter("controlBank");
        filter.setAuditFieldList(Arrays.asList(AuditFieldList.ALL.getId()));

        long count = auditDao.getCountForControlUnp(filter);
        assertEquals(2, count);
    }

    @Test
    public void testCountForOper() throws ParseException {
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(SIMPLE_DATE_FORMAT.parse("01.05.2011"));
        filter.setToSearchDate(SIMPLE_DATE_FORMAT.parse("30.05.2013"));
        filter.setFilter("controlBank");
        filter.setAuditFieldList(Arrays.asList(AuditFieldList.ALL.getId()));

        long count = auditDao.getCountForOper(filter,
                new HashMap<AuditDao.SAMPLE_NUMBER, Collection<Integer>>() {{
                    put(AuditDao.SAMPLE_NUMBER.S_55, Arrays.asList(10));
                    put(AuditDao.SAMPLE_NUMBER.S_45, Arrays.asList(10));
                    put(AuditDao.SAMPLE_NUMBER.S_10, Arrays.asList(10));
                }});
        assertEquals(2, count);
    }

    //@Test
    public void testGet() throws ParseException {
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(SIMPLE_DATE_FORMAT.parse("01.05.2011"));
        filter.setToSearchDate(SIMPLE_DATE_FORMAT.parse("30.05.2013"));
        filter.setFilter("controlBank");
        filter.setAuditFieldList(Arrays.asList(AuditFieldList.ALL.getId()));

        PagingResult<LogSearchResultItem> records = auditDao.getLogsForAdmin(filter);
        LogSearchResultItem logSystem = records.get(0);
        assertEquals(Long.valueOf(3), logSystem.getId());
        assertEquals("192.168.72.16", logSystem.getIp());
        assertEquals(FormDataEvent.getByCode(601), logSystem.getEvent());
        assertEquals("controlBank", logSystem.getUser());
        assertEquals("operator", logSystem.getRoles());
        assertEquals("2013 первый квартал", logSystem.getReportPeriodName());
        assertEquals("test form_type_name", logSystem.getFormTypeName());
        assertEquals(3, logSystem.getFormKind().getId());
        assertEquals("the best note", logSystem.getNote());
        assertEquals("Подразделение", logSystem.getUserDepartmentName());
        assertEquals(3, records.getTotalCount());
    }

	@Test
	public void getLogsNull() {
		LogSystemFilter filter = new LogSystemFilter();
		filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(null);
        filter.setToSearchDate(null);
        filter.setFilter("Transport");
        filter.setAuditFieldList(Arrays.asList(AuditFieldList.ALL.getId()));

		long count = auditDao.getCount(filter);
		assertEquals(1, count);
	}

    @Test
    @Transactional(readOnly = false)
    public void testRemove2(){
        LogSystem logSystem = new LogSystem();
        logSystem.setId(100l);
        logSystem.setIp("192.168.72.16");
        logSystem.setEventId(3);
        logSystem.setUserLogin("controlBank");
        logSystem.setRoles("operator");
        logSystem.setFormDepartmentName("ТБ1");
        logSystem.setReportPeriodName("2014 полугодие");
        logSystem.setDeclarationTypeName("test DeclarationType");
        logSystem.setFormTypeName("test FormType");
        logSystem.setFormKindId(2);
        logSystem.setNote("the best note");
        logSystem.setUserDepartmentName("Подразделение");

        List<Long> listIds = new ArrayList<Long>();
        listIds.add(1l);
        listIds.add(3l);

        auditDao.add(logSystem);
        auditDao.removeRecords(listIds);

        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(new Date(1304247365000l));
        filter.setToSearchDate(new Date());
        filter.setFilter("");
        filter.setAuditFieldList(new ArrayList<Long>());

        long count = auditDao.getCount(filter);
        assertEquals(3, count);
    }

    @Test
    public void testGetDate(){
        assertNotNull(auditDao.lastArchiveDate());
    }

    //@Test
    public void testGetLogBusinessForOper(){
        Calendar calendar = Calendar.getInstance();
        LogSystemFilter filter = new LogSystemFilter();
        calendar.set(2012, Calendar.JANUARY, 1);
        filter.setFromSearchDate(calendar.getTime());
        calendar.set(2014, Calendar.DECEMBER, 31);
        filter.setToSearchDate(calendar.getTime());
        filter.setCountOfRecords(5);
        filter.setSearchOrdering(HistoryBusinessSearchOrdering.DATE);

        LogSystemFilter filter2 = new LogSystemFilter();
        filter2.setFilter("controlBank");
        filter2.setAuditFieldList(Arrays.asList(AuditFieldList.USER.getId()));
        filter.setOldLogSystemFilter(filter2);

        LogSystemFilter filter3 = new LogSystemFilter();
        filter3.setFilter("2013 первый квартал");
        filter3.setAuditFieldList(Arrays.asList(AuditFieldList.PERIOD.getId()));
        filter2.setOldLogSystemFilter(filter3);

        HashMap<AuditDao.SAMPLE_NUMBER, Collection<Integer>> sampleVal =
                new HashMap<AuditDao.SAMPLE_NUMBER, Collection<Integer>>(3);
        sampleVal.put(AuditDao.SAMPLE_NUMBER.S_10, new ArrayList<Integer>(1){{add(1);}});
        sampleVal.put(AuditDao.SAMPLE_NUMBER.S_45, new ArrayList<Integer>(1){{add(1);}});
        sampleVal.put(AuditDao.SAMPLE_NUMBER.S_55, new ArrayList<Integer>(1){{add(1);}});
        PagingResult<LogSearchResultItem> records = auditDao.getLogsBusinessForOper(filter, sampleVal);
        assertEquals(2, records.size());
    }


    @Test
    @Transactional(readOnly = false)
    public void testAddNull() {
        LogSystem logSystem = new LogSystem();
        logSystem.setId(10l);
        logSystem.setIp("192.168.72.16");
        logSystem.setUserLogin("controlBank");
        logSystem.setRoles("operator");
        logSystem.setFormDepartmentName("ТБ");
        logSystem.setReportPeriodName(null);
        logSystem.setDeclarationTypeName(null);
        logSystem.setFormTypeName(null);
        logSystem.setFormKindId(null);
        logSystem.setNote("the best note");
        logSystem.setUserDepartmentName("Подразделение");

        auditDao.add(logSystem);
    }

    @Test
    public void testCutOffLogSystemFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = AuditDaoImpl.class.getDeclaredMethod("cutOffLogSystemFilter", LogSystemFilter.class);
        method.setAccessible(true);

        LogSystemFilter logSystemAuditFilter = new LogSystemFilter();

        method.invoke(auditDao, logSystemAuditFilter);
        assertTrue(logSystemAuditFilter.getFilter() == null);
    }

    @Test
    public void testRestrictFilterLength() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = AuditDaoImpl.class.getDeclaredMethod("cutOffLogSystemFilter", LogSystemFilter.class);
        method.setAccessible(true);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < FILTER_LENGTH + 1; i++) {
            stringBuilder.append("a");
        }

        LogSystemFilter logSystemFilter = new LogSystemFilter();
        logSystemFilter.setFilter(stringBuilder.toString());

        method.invoke(auditDao, logSystemFilter);
        assertTrue(logSystemFilter.getFilter().length() <= FILTER_LENGTH);
    }

    @Test
    @Transactional(readOnly = false)
    public void testRemove(){
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(new Date(1304247365000l));
        filter.setToSearchDate(new Date());
        filter.setFilter("");
        filter.setAuditFieldList(new ArrayList<Long>());

        auditDao.removeRecords(filter);
    }

    @Test
    public void firstDateTest(){
        assertEquals("2013-01-01 01:00:00.0", auditDao.firstDateOfLog().toString());
    }

}
