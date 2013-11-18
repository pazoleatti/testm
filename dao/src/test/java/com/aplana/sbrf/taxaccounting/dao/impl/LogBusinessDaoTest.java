package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"LogBusinessDaoTest.xml"})
@Transactional
public class LogBusinessDaoTest {

	@Autowired
	private LogBusinessDao logBusinessDao;

	@Test
	public void testDeclarationGet() {
		LogBusiness logBusiness = logBusinessDao.getDeclarationLogsBusiness(1).get(0);
		assertEquals(Long.valueOf(1), logBusiness.getId());
		assertEquals(1, logBusiness.getEventId());
		assertEquals(1, logBusiness.getUserId());
		assertEquals("operator", logBusiness.getRoles());
		assertEquals(Long.valueOf(1), logBusiness.getDeclarationId());
		assertEquals(null, logBusiness.getFormId());
		assertEquals(1, logBusiness.getDepartmentId());
		assertEquals("the best note", logBusiness.getNote());
	}

	@Test
	public void testDeclarationAdd() {
		LogBusiness logBusiness = new LogBusiness();
		logBusiness.setId(3l);
		logBusiness.setLogDate(new Date());
		logBusiness.setDeclarationId(1l);
		logBusiness.setEventId(3);
		logBusiness.setUserId(1);
		logBusiness.setRoles("operator");
		logBusiness.setDepartmentId(2);
		logBusiness.setNote("the best note");
		logBusinessDao.add(logBusiness);

		logBusiness = logBusinessDao.getDeclarationLogsBusiness(1).get(0);
		assertEquals(Long.valueOf(3), logBusiness.getId());
		assertEquals(3, logBusiness.getEventId());
		assertEquals(1, logBusiness.getUserId());
		assertEquals("operator", logBusiness.getRoles());
		assertEquals(Long.valueOf(1), logBusiness.getDeclarationId());
		assertEquals(null, logBusiness.getFormId());
		assertEquals(2, logBusiness.getDepartmentId());
		assertEquals("the best note", logBusiness.getNote());
	}

	@Test
	public void testFormGet() {
		LogBusiness logBusiness = logBusinessDao.getFormLogsBusiness(1).get(0);
		assertEquals(Long.valueOf(2), logBusiness.getId());
		assertEquals(2, logBusiness.getEventId());
		assertEquals(1, logBusiness.getUserId());
		assertEquals("operator", logBusiness.getRoles());
		assertEquals(null, logBusiness.getDeclarationId());
		assertEquals(Long.valueOf(1), logBusiness.getFormId());
		assertEquals(2, logBusiness.getDepartmentId());
		assertEquals("the best note", logBusiness.getNote());
	}

	@Test
	public void testFormAdd() {
		logBusinessDao.add(createFormLogBusiness(3, 3l ,new Date()));
		LogBusiness logBusiness = logBusinessDao.getFormLogsBusiness(1).get(0);
		assertEquals(Long.valueOf(3), logBusiness.getId());
		assertEquals(3, logBusiness.getEventId());
		assertEquals(1, logBusiness.getUserId());
		assertEquals("operator", logBusiness.getRoles());
		assertEquals(null, logBusiness.getDeclarationId());
		assertEquals(Long.valueOf(1), logBusiness.getFormId());
		assertEquals(1, logBusiness.getDepartmentId());
		assertEquals("the best note", logBusiness.getNote());
	}

	@Test
	public void testGetFormDates() {
		logBusinessDao.add(createFormLogBusiness(FormDataEvent.MOVE_APPROVED_TO_ACCEPTED.getCode(), 4l, new Date(13253454586354l)));
		logBusinessDao.add(createFormLogBusiness(FormDataEvent.MOVE_CREATED_TO_ACCEPTED.getCode(), 5l, new Date(14253454568354l)));
		logBusinessDao.add(createFormLogBusiness(FormDataEvent.CREATE.getCode(), 6l, new Date(13253454568354l)));
		logBusinessDao.add(createFormLogBusiness(FormDataEvent.MOVE_PREPARED_TO_ACCEPTED.getCode(), 7l, new Date(12253456453854l)));
		Date acceptanceDate = logBusinessDao.getFormAcceptanceDate(1);
		Date creationDate = logBusinessDao.getFormCreationDate(1);
		assertEquals(new Date(14253454568000l).getTime(), acceptanceDate.getTime());
		assertEquals(new Date(13253454568000l).getTime(), creationDate.getTime());
	}

    @Test
    public void testGetLogsBusiness() {
        LogBusinessFilterValuesDao filterValuesDao = new LogBusinessFilterValuesDao();
        filterValuesDao.setDepartmentId(1);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 0);
        filterValuesDao.setFromSearchDate(calendar.getTime());
        calendar.set(2013, Calendar.JANUARY, 1);
        filterValuesDao.setToSearchDate(calendar.getTime());
        filterValuesDao.setStartIndex(0);
        filterValuesDao.setCountOfRecords(1);
        filterValuesDao.setDepartmentId(1);
        filterValuesDao.setUserId(1l);
        List<Long> formDataIds = new ArrayList<Long>();
        formDataIds.add(1l);
        List<Long> declarationIds = new ArrayList<Long>();
        declarationIds.add(1l);
        declarationIds.add(2l);

        PagingResult<LogSystemSearchResultItem> resultItems = logBusinessDao.getLogsBusiness(formDataIds, declarationIds, filterValuesDao);
        assertEquals(1, resultItems.getTotalCount());
        assertEquals(1, resultItems.size());
        /*assertEquals("Банк", resultItems.get(0).getDepartment().getName());*/
    }

	private LogBusiness createFormLogBusiness(int event_id, long id, Date date) {
		LogBusiness logBusiness = new LogBusiness();
		logBusiness.setId(id);
		logBusiness.setLogDate(date);
		logBusiness.setFormId(1l);
		logBusiness.setEventId(event_id);
		logBusiness.setUserId(1);
		logBusiness.setRoles("operator");
		logBusiness.setDepartmentId(1);
		logBusiness.setNote("the best note");
		return logBusiness;
	}

}
