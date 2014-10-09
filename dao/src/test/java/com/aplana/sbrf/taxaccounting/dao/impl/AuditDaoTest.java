package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.LogSystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"AuditDaoTest.xml"})
@Transactional(readOnly = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuditDaoTest {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    public static final int LENGTH = 1000;

    @Autowired
    private AuditDao auditDao;

    @Test
    @Transactional(readOnly = false)
    public void testAdd() {
        LogSystem logSystem = new LogSystem();
        logSystem.setId(10l);
        Date date = new Date();
        logSystem.setLogDate(date);
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

        auditDao.add(logSystem);
    }

    /*@Test
    public void testGet() {
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(new Date(1304247365000l));
        filter.setToSearchDate(new Date(1369911365000l));
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

		PagingResult<LogSearchResultItem> records = auditDao.getLogsForAdmin(filter);
		assertFalse(records.isEmpty());
	}

    @Test
    @Transactional(readOnly = false)
    public void testRemove(){
        LogSystem logSystem = new LogSystem();
        logSystem.setId(100l);
        Date date = new Date();
        logSystem.setLogDate(date);
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

        PagingResult<LogSearchResultItem> records = auditDao.getLogsForAdmin(filter);
        assertEquals(2, records.size());
    }

    @Test
    public void testGetDate(){
        assertNotNull(auditDao.lastArchiveDate());
    }

    @Test
    public void testGetLogBusiness(){
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

        PagingResult<LogSearchResultItem> records = auditDao.getLogsBusinessForControl(filter, null, null);
        assertEquals(2, records.size());
    }

    @Test
    @Transactional(readOnly = false)
    public void testAddNull() {
        LogSystem logSystem = new LogSystem();
        logSystem.setId(10l);
        Date date = new Date();
        logSystem.setLogDate(date);
        logSystem.setIp("192.168.72.16");
        logSystem.setEventId(FormDataEvent.MIGRATION.getCode());
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

        LogSystemFilter restrictedLogSystemAuditFilter = (LogSystemFilter) method.invoke(auditDao, logSystemAuditFilter);
        assertTrue(restrictedLogSystemAuditFilter.getFilter() == null);
    }

    @Test
    public void testRestrictFilterLength() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = AuditDaoImpl.class.getDeclaredMethod("cutOffLogSystemFilter", LogSystemFilter.class);
        method.setAccessible(true);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < LENGTH + 1; i++) {
            stringBuilder.append("a");
        }

        LogSystemFilter logSystemFilter = new LogSystemFilter();
        logSystemFilter.setFilter(stringBuilder.toString());

        LogSystemFilter restrictedLogSystemAuditFilter = (LogSystemFilter) method.invoke(auditDao, logSystemFilter);
        assertTrue(restrictedLogSystemAuditFilter.getFilter().length() <= LENGTH);
    }*/

}
