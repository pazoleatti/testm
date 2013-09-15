package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"AuditDaoTest.xml"})
@Transactional
public class AuditDaoTest {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private AuditDao auditDao;

    @Test
    public void testGet() {
        LogSystemFilter filter = new LogSystemFilter();
        filter.setCountOfRecords(10);
        filter.setStartIndex(0);
        filter.setFormTypeId(1);
        filter.setFromSearchDate(new Date(1304247365000l));
        filter.setToSearchDate(new Date(1369911365000l));

        PagingResult<LogSystemSearchResultItem> records = auditDao.getLogs(filter);
        LogSystemSearchResultItem logSystem = records.get(0);
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
        filter.setUserId(1);
        filter.setFromSearchDate(new Date(1304247365000l));
        filter.setToSearchDate(new Date());

        PagingResult<LogSystemSearchResultItem> records = auditDao.getLogs(filter);
        LogSystemSearchResultItem logSystemSearchResultItem = records.get(0);
        assertEquals(Long.valueOf(3), logSystemSearchResultItem.getId());
        assertEquals(formatter.format(date), formatter.format(logSystemSearchResultItem.getLogDate()));
        assertEquals("192.168.72.16", logSystemSearchResultItem.getIp());
        assertEquals(3, logSystemSearchResultItem.getEvent().getCode());
        assertEquals(1, logSystemSearchResultItem.getUser().getId());
        assertEquals("operator", logSystemSearchResultItem.getRoles());
        assertEquals(1, logSystemSearchResultItem.getDepartment().getId());
        assertEquals(1, logSystemSearchResultItem.getReportPeriod().getId().intValue());
        assertEquals(1, logSystemSearchResultItem.getDeclarationType().getId());
        assertEquals(1, logSystemSearchResultItem.getFormType().getId());
        assertEquals(2, logSystemSearchResultItem.getFormKind().getId());
        assertEquals("the best note", logSystemSearchResultItem.getNote());
        assertEquals(1, logSystemSearchResultItem.getUserDepartment().getId());
        assertEquals(2, records.getTotalCount());
    }
}
