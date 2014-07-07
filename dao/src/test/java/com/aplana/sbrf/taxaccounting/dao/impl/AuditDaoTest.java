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

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"AuditDaoTest.xml"})
@Transactional(readOnly = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuditDaoTest {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private AuditDao auditDao;

    @Test
    public void testGet() {
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(new Date(1304247365000l));
        filter.setToSearchDate(new Date(1369911365000l));
        filter.setFilter("controlBank");
        filter.setAuditFieldList(Arrays.asList(AuditFieldList.ALL.getId()));

        PagingResult<LogSearchResultItem> records = auditDao.getLogs(filter);
/*        LogSearchResultItem logSystem = records.get(0);
        assertEquals(Long.valueOf(3), logSystem.getId());
        assertEquals("192.168.72.16", logSystem.getIp());
        assertEquals(FormDataEvent.getByCode(601), logSystem.getEvent());
        assertEquals("controlBank"testAdd, logSystem.getUser());
        assertEquals("operator", logSystem.getRoles());
        assertEquals("2013 первый квартал", logSystem.getReportPeriodName());
        assertEquals("FormType - Transport", logSystem.getFormTypeName());
        assertEquals(3, logSystem.getFormKind().getId());
        assertEquals("the best note", logSystem.getNote());
        assertEquals("Подразделение", logSystem.getUserDepartmentName());
        assertEquals(1, records.getTotalCount());*/
    }

    @Test
    @Transactional(readOnly = false)
    public void testAdd() {
        LogSystem logSystem = new LogSystem();
        logSystem.setId(10l);
        Date date = new Date();
        logSystem.setLogDate(date);
        logSystem.setIp("192.168.72.16");
        logSystem.setEventId(3);
        logSystem.setUserId(1);
        logSystem.setRoles("operator");
        logSystem.setFormDepartmentName("Подразделение");
        logSystem.setReportPeriodName("2013 первый квартал");
        logSystem.setDeclarationTypeName("test DeclarationType");
        logSystem.setFormTypeName("test FormType");
        logSystem.setFormKindId(2);
        logSystem.setNote("the best note");
        logSystem.setUserDepartmentName("Подразделение");

        auditDao.add(logSystem);

        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFromSearchDate(new Date(1304247365000l));
        filter.setToSearchDate(new Date());
        filter.setFilter("controlBank");
        filter.setAuditFieldList(Arrays.asList(AuditFieldList.USER.getId()));

        PagingResult<LogSearchResultItem> records = auditDao.getLogs(filter);

        LogSearchResultItem logSearchResultItem = records.get(0);
        assertEquals(Long.valueOf(10), logSearchResultItem.getId());
        assertEquals(formatter.format(date), formatter.format(logSearchResultItem.getLogDate()));
        assertEquals("192.168.72.16", logSearchResultItem.getIp());
        assertEquals(3, logSearchResultItem.getEvent().getCode());
        assertEquals("controlBank", logSearchResultItem.getUser());
        assertEquals("operator", logSearchResultItem.getRoles());
        assertEquals("2013 первый квартал", logSearchResultItem.getReportPeriodName());
        assertEquals("test DeclarationType", logSearchResultItem.getDeclarationTypeName());
        assertEquals("test FormType", logSearchResultItem.getFormTypeName());
        assertEquals(2, logSearchResultItem.getFormKind().getId());
        assertEquals("the best note", logSearchResultItem.getNote());
        assertEquals("Подразделение", logSearchResultItem.getUserDepartmentName());
        assertEquals(4, records.getTotalCount());
    }

	@Test
	public void getLogsNull() {
		LogSystemFilter filter = new LogSystemFilter();
//		filter.setCountOfRecords(10);
//		filter.setStartIndex(0);
//		filter.setFormTypeId(1);
		filter.setFromSearchDate(null);
		filter.setToSearchDate(null);
        filter.setFilter("Transport");
        filter.setAuditFieldList(Arrays.asList(AuditFieldList.ALL.getId()));

		PagingResult<LogSearchResultItem> records = auditDao.getLogs(filter);
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
        logSystem.setUserId(1);
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

        PagingResult<LogSearchResultItem> records = auditDao.getLogs(filter);
        assertEquals(2, records.size());
    }

    @Test
    public void testGetDate(){
        assertNotNull(auditDao.lastArchiveDate());
    }

    //@Test
    public void testGetLogBusiness(){
        Calendar calendar = Calendar.getInstance();
        LogSystemFilterDao filterDao = new LogSystemFilterDao();
        filterDao.setDepartmentName("ТБ");        
        calendar.set(2012, Calendar.JANUARY, 1);
        filterDao.setFromSearchDate(calendar.getTime());
        calendar.set(2014, Calendar.DECEMBER, 31);
        filterDao.setToSearchDate(calendar.getTime());
        filterDao.setCountOfRecords(5);
        filterDao.setSearchOrdering(HistoryBusinessSearchOrdering.DATE);
        filterDao.setReportPeriodName("2014 первый квартал");
        filterDao.setUserIds(Arrays.asList(1l));
        //PagingResult<LogSearchResultItem> records = auditDao.getLogsBusiness(filterDao, null);
        //assertEquals(0, records.size());
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
        logSystem.setUserId(1);
        logSystem.setRoles("operator");
        logSystem.setFormDepartmentName("");
        logSystem.setReportPeriodName(null);
        logSystem.setDeclarationTypeName(null);
        logSystem.setFormTypeName(null);
        logSystem.setFormKindId(null);
        logSystem.setNote("the best note");
        logSystem.setUserDepartmentName("Подразделение");

        auditDao.add(logSystem);
    }

}
