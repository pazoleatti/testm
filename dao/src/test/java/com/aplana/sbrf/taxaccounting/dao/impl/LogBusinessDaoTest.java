package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"LogBusinessDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LogBusinessDaoTest {

	@Autowired
	private LogBusinessDao logBusinessDao;

    private static String LOGIN_CONTROL_BANK = "controlBank";

	@Test
	public void testDeclarationGet() {
		LogBusiness logBusiness = logBusinessDao.getDeclarationLogsBusiness(1, HistoryBusinessSearchOrdering.DATE, false).get(0);
		assertEquals(Long.valueOf(1), logBusiness.getId());
		assertEquals(1, logBusiness.getEventId());
		assertEquals(LOGIN_CONTROL_BANK, logBusiness.getUserLogin());
		assertEquals("operator", logBusiness.getRoles());
		assertEquals(Long.valueOf(1), logBusiness.getDeclarationId());
		assertEquals(null, logBusiness.getFormId());
		assertEquals("А - департамент", logBusiness.getDepartmentName());
		assertEquals("the best note", logBusiness.getNote());
	}

	@Test
	public void testDeclarationAdd() {
		LogBusiness logBusiness = new LogBusiness();
		logBusiness.setId(3l);
		logBusiness.setLogDate(new Date());
		logBusiness.setDeclarationId(1l);
		logBusiness.setEventId(3);
		logBusiness.setUserLogin(LOGIN_CONTROL_BANK);
		logBusiness.setRoles("operator");
		logBusiness.setDepartmentName("Б - департамент");
		logBusiness.setNote("the best note");
		logBusinessDao.add(logBusiness);

		logBusiness = logBusinessDao.getDeclarationLogsBusiness(1, HistoryBusinessSearchOrdering.DATE, false).get(0);
		assertEquals(Long.valueOf(3), logBusiness.getId());
		assertEquals(3, logBusiness.getEventId());
		assertEquals(LOGIN_CONTROL_BANK, logBusiness.getUserLogin());
		assertEquals("operator", logBusiness.getRoles());
		assertEquals(Long.valueOf(1), logBusiness.getDeclarationId());
		assertEquals(null, logBusiness.getFormId());
		assertEquals("Б - департамент", logBusiness.getDepartmentName());
		assertEquals("the best note", logBusiness.getNote());
	}

	@Test
	public void testFormGet() {
		LogBusiness logBusiness = logBusinessDao.getFormLogsBusiness(1, HistoryBusinessSearchOrdering.DATE, false).get(0);
		assertEquals(Long.valueOf(2), logBusiness.getId());
		assertEquals(2, logBusiness.getEventId());
		assertEquals(LOGIN_CONTROL_BANK, logBusiness.getUserLogin());
		assertEquals("operator", logBusiness.getRoles());
		assertEquals(null, logBusiness.getDeclarationId());
		assertEquals(Long.valueOf(1), logBusiness.getFormId());
		assertEquals("Б - департамент", logBusiness.getDepartmentName());
		assertEquals("the best note", logBusiness.getNote());
	}

	@Test
	public void testFormAdd() {
		logBusinessDao.add(createFormLogBusiness(3, 3l));
		LogBusiness logBusiness = logBusinessDao.getFormLogsBusiness(1, HistoryBusinessSearchOrdering.DATE, false).get(0);
		assertEquals(Long.valueOf(3), logBusiness.getId());
		assertEquals(3, logBusiness.getEventId());
        assertEquals(LOGIN_CONTROL_BANK, logBusiness.getUserLogin());
		assertEquals("operator", logBusiness.getRoles());
		assertEquals(null, logBusiness.getDeclarationId());
		assertEquals(Long.valueOf(1), logBusiness.getFormId());
		assertEquals("А - департамент", logBusiness.getDepartmentName());
		assertEquals("the best note", logBusiness.getNote());
	}

	@Test
	public void testGetFormDates() {
		logBusinessDao.add(createFormLogBusiness(FormDataEvent.MOVE_APPROVED_TO_ACCEPTED.getCode(), 4l));
		logBusinessDao.add(createFormLogBusiness(FormDataEvent.MOVE_CREATED_TO_ACCEPTED.getCode(), 5l));
		logBusinessDao.add(createFormLogBusiness(FormDataEvent.CREATE.getCode(), 6l));
		logBusinessDao.add(createFormLogBusiness(FormDataEvent.MOVE_PREPARED_TO_ACCEPTED.getCode(), 7l));

        /*Date acceptanceDate = logBusinessDao.getFormAcceptanceDate(1);
		Date creationDate = logBusinessDao.getFormCreationDate(1);
		assertEquals(new Date(14253454568000l).getTime(), acceptanceDate.getTime());
		assertEquals(new Date(13253454568000l).getTime(), creationDate.getTime());*/
	}

	private LogBusiness createFormLogBusiness(int event_id, long id) {
		LogBusiness logBusiness = new LogBusiness();
		logBusiness.setId(id);
		logBusiness.setFormId(1l);
		logBusiness.setEventId(event_id);
		logBusiness.setUserLogin(LOGIN_CONTROL_BANK);
		logBusiness.setRoles("operator");
		logBusiness.setDepartmentName("А - департамент");
		logBusiness.setNote("the best note");
		return logBusiness;
	}

	@Test
	public void testGetUserLoginImportTf() {
		LogBusiness logBusiness = new LogBusiness();
		logBusiness.setId(5L);
		logBusiness.setLogDate(new Date());
		logBusiness.setDeclarationId(1l);
		logBusiness.setEventId(401);
		logBusiness.setUserLogin(LOGIN_CONTROL_BANK);
		logBusiness.setRoles("operator");
		logBusiness.setDepartmentName("Б - департамент");
		logBusiness.setNote("the best note");
		logBusinessDao.add(logBusiness);

		assertEquals(LOGIN_CONTROL_BANK, logBusinessDao.getFormCreationUserName(1));
	}
}
