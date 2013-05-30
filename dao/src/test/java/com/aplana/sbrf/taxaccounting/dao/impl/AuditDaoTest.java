package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.LogSystem;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;
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
		filter.setFormTypeId(1);
		filter.setFromSearchDate(new Date(1304247365000l));
		filter.setToSearchDate(new Date(1369911365000l));

		LogSystem logSystem = auditDao.getLogs(filter).get(0);
		assertEquals(Long.valueOf(1), logSystem.getId());
		assertEquals(1, logSystem.getEventId());
		assertEquals(1, logSystem.getUserId());
		assertEquals("operator", logSystem.getRoles());
		assertEquals(1, logSystem.getDepartmentId());
		assertEquals(1, logSystem.getReportPeriodId());
		assertEquals(1, logSystem.getDeclarationTypeId());
		assertEquals(1, logSystem.getFormTypeId());
		assertEquals(1, logSystem.getFormKindId());
		assertEquals("the best note", logSystem.getNote());
		assertEquals(1, logSystem.getUserDepartmentId());
	}

	@Test
	public void testAdd() {
		LogSystem logSystem = new LogSystem();
		logSystem.setId(3l);
		Date date = new Date();
		logSystem.setLogDate(date);
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
		filter.setFormTypeId(1);
		filter.setUserId(1);
		filter.setFromSearchDate(new Date(1304247365000l));
		filter.setToSearchDate(new Date());

		logSystem = auditDao.getLogs(filter).get(1);
		assertEquals(Long.valueOf(3), logSystem.getId());
		assertEquals(formatter.format(date), formatter.format(logSystem.getLogDate()));
		assertEquals(3, logSystem.getEventId());
		assertEquals(1, logSystem.getUserId());
		assertEquals("operator", logSystem.getRoles());
		assertEquals(1, logSystem.getDepartmentId());
		assertEquals(1, logSystem.getReportPeriodId());
		assertEquals(1, logSystem.getDeclarationTypeId());
		assertEquals(1, logSystem.getFormTypeId());
		assertEquals(2, logSystem.getFormKindId());
		assertEquals("the best note", logSystem.getNote());
		assertEquals(1, logSystem.getUserDepartmentId());
	}
}
