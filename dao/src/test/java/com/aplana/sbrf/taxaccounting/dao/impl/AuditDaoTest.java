package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.test.BDUtilsMock;
import com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"AuditDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuditDaoTest {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private AuditDao auditDao;

    @Before
    public void init() {
        Department dep = new Department();
        dep.setId(1);
        DepartmentDao departmentDao = mock(DepartmentDao.class);
        when(departmentDao.getParentsHierarchy(1)).thenReturn("Подразделение");
        when(departmentDao.getDepartment(1)).thenReturn(dep);
        ReflectionTestUtils.setField(auditDao, "departmentDao", departmentDao);
    }

    @Test
    public void testGet() {
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFormTypeId(1);
        filter.setFromSearchDate(new Date(1304247365000l));
        filter.setToSearchDate(new Date(1369911365000l));
        filter.setTaxType(TaxType.TRANSPORT);

        PagingResult<LogSearchResultItem> records = auditDao.getLogs(filter);
        LogSearchResultItem logSystem = records.get(0);
        assertEquals(Long.valueOf(1), logSystem.getId());
        assertEquals("192.168.72.16", logSystem.getIp());
        assertEquals(FormDataEvent.getByCode(1), logSystem.getEvent());
        assertEquals(1, logSystem.getUser().getId());
        assertEquals("operator", logSystem.getRoles());
        assertEquals(1, logSystem.getDepartment().getId());
        assertEquals(1, logSystem.getReportPeriod().getId().intValue());
        assertEquals(1, logSystem.getDeclarationType().getId());
        assertEquals(1, logSystem.getFormType().getId());
        assertEquals(1, logSystem.getFormKind().getId());
        assertEquals("the best note", logSystem.getNote());
        assertEquals(1, logSystem.getUserDepartment().getId());
        assertEquals(1, records.getTotalCount());
    }

    @Test
    public void testAdd() {
        LogSystem logSystem = new LogSystem();
        logSystem.setId(3l);
        Date date = new Date();
        logSystem.setLogDate(date);
        logSystem.setIp("192.168.72.16");
        logSystem.setEventId(3);
        logSystem.setUserId(1);
        logSystem.setRoles("operator");
        logSystem.setDepartmentId(1);
        logSystem.setReportPeriodId(1);
        logSystem.setDeclarationTypeId(1);
        logSystem.setFormTypeId(1);
        logSystem.setFormKindId(2);
        logSystem.setNote("the best note");
        logSystem.setUserDepartmentId(1);

        auditDao.add(logSystem);

        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFormTypeId(1);
        List<Long> userList = new ArrayList<Long>();
        userList.add(1L);
        filter.setUserIds(userList);
        filter.setFromSearchDate(new Date(1304247365000l));
        filter.setToSearchDate(new Date());

        PagingResult<LogSearchResultItem> records = auditDao.getLogs(filter);
        LogSearchResultItem logSearchResultItem = records.get(0);
        assertEquals(Long.valueOf(3), logSearchResultItem.getId());
        assertEquals(formatter.format(date), formatter.format(logSearchResultItem.getLogDate()));
        assertEquals("192.168.72.16", logSearchResultItem.getIp());
        assertEquals(3, logSearchResultItem.getEvent().getCode());
        assertEquals(1, logSearchResultItem.getUser().getId());
        assertEquals("operator", logSearchResultItem.getRoles());
        assertEquals(1, logSearchResultItem.getDepartment().getId());
        assertEquals(1, logSearchResultItem.getReportPeriod().getId().intValue());
        assertEquals(1, logSearchResultItem.getDeclarationType().getId());
        assertEquals(1, logSearchResultItem.getFormType().getId());
        assertEquals(2, logSearchResultItem.getFormKind().getId());
        assertEquals("the best note", logSearchResultItem.getNote());
        assertEquals(1, logSearchResultItem.getUserDepartment().getId());
        assertEquals(2, records.getTotalCount());
    }

	@Test
	public void getLogsNull() {
		LogSystemFilter filter = new LogSystemFilter();
//		filter.setCountOfRecords(10);
//		filter.setStartIndex(0);
//		filter.setFormTypeId(1);
		filter.setFromSearchDate(null);
		filter.setToSearchDate(null);
		filter.setTaxType(TaxType.TRANSPORT);

		PagingResult<LogSearchResultItem> records = auditDao.getLogs(filter);
		assertFalse(records.isEmpty());
	}

    @Test
    public void testRemove(){
        LogSystem logSystem = new LogSystem();
        logSystem.setId(3l);
        Date date = new Date();
        logSystem.setLogDate(date);
        logSystem.setIp("192.168.72.16");
        logSystem.setEventId(3);
        logSystem.setUserId(1);
        logSystem.setRoles("operator");
        logSystem.setDepartmentId(1);
        logSystem.setReportPeriodId(1);
        logSystem.setDeclarationTypeId(1);
        logSystem.setFormTypeId(1);
        logSystem.setFormKindId(2);
        logSystem.setNote("the best note");
        logSystem.setUserDepartmentId(1);

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

        assertEquals(1, auditDao.getLogs(filter).size());
    }

    @Test
    public void testGetDate(){
        assertNotNull(auditDao.lastArchiveDate());
    }
}
