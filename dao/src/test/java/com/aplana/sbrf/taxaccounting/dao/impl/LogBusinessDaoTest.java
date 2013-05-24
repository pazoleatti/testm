package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"LogBusinessDaoTest.xml"})
@Transactional
public class LogBusinessDaoTest {

	@Autowired
	private LogBusinessDao logBusinessDao;

	@Test
	public void testDeclarationGet() {
		LogBusiness d = logBusinessDao.getDeclarationLogsBusiness(1).get(0);
		assertEquals(Long.valueOf(1), d.getId());
		assertEquals(1, d.getEventId());
		assertEquals(1, d.getUserId());
		assertEquals("operator", d.getRoles());
		assertEquals(Long.valueOf(1), d.getDeclarationId());
		assertEquals(null, d.getFormId());
		assertEquals(1, d.getDepartmentId());
		assertEquals("the best note", d.getNote());
	}

	@Test
	public void testDeclarationAdd() {
		LogBusiness d = new LogBusiness();
		d.setId(3l);
		d.setLogDate(new Date());
		d.setDeclarationId(1l);
		d.setEventId(3);
		d.setUserId(1);
		d.setRoles("operator");
		d.setDepartmentId(2);
		d.setNote("the best note");
		logBusinessDao.add(d);

		d = logBusinessDao.getDeclarationLogsBusiness(1).get(0);
		assertEquals(Long.valueOf(3), d.getId());
		assertEquals(3, d.getEventId());
		assertEquals(1, d.getUserId());
		assertEquals("operator", d.getRoles());
		assertEquals(Long.valueOf(1), d.getDeclarationId());
		assertEquals(null, d.getFormId());
		assertEquals(2, d.getDepartmentId());
		assertEquals("the best note", d.getNote());
	}

	@Test
	public void testFormGet() {
		LogBusiness f = logBusinessDao.getFormLogsBusiness(1).get(0);
		assertEquals(Long.valueOf(2), f.getId());
		assertEquals(1, f.getEventId());
		assertEquals(1, f.getUserId());
		assertEquals("operator", f.getRoles());
		assertEquals(null, f.getDeclarationId());
		assertEquals(Long.valueOf(1), f.getFormId());
		assertEquals(2, f.getDepartmentId());
		assertEquals("the best note", f.getNote());
	}

	@Test
	public void testFormAdd() {
		LogBusiness f = new LogBusiness();
		f.setId(3l);
		f.setLogDate(new Date());
		f.setFormId(1l);
		f.setEventId(3);
		f.setUserId(1);
		f.setRoles("operator");
		f.setDepartmentId(1);
		f.setNote("the best note");
		logBusinessDao.add(f);

		f = logBusinessDao.getFormLogsBusiness(1).get(0);
		assertEquals(Long.valueOf(3), f.getId());
		assertEquals(3, f.getEventId());
		assertEquals(1, f.getUserId());
		assertEquals("operator", f.getRoles());
		assertEquals(null, f.getDeclarationId());
		assertEquals(Long.valueOf(1), f.getFormId());
		assertEquals(1, f.getDepartmentId());
		assertEquals("the best note", f.getNote());
	}
}
