package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;

@Ignore("Налоговые формы не используются!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormPerformerDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FormPerformerDaoTest {
	@Autowired
	private FormPerformerDao formPerformerDao;

	@Test
	public void getTest() {
		FormDataPerformer performer = formPerformerDao.get(1);
		assertEquals("name1", performer.getName());
        assertEquals("phone1", performer.getPhone());
        assertEquals(Integer.valueOf(2), performer.getPrintDepartmentId());
	}

	@Test
	public void getNotFoundTest() {
		FormDataPerformer performer = formPerformerDao.get(2);
		assertNull(performer);
	}

	@Test
	public void saveUpdateTest() {
		FormDataPerformer performer = new FormDataPerformer();
		performer.setName("MyNameUpdate");
		performer.setPhone("MyPhoneUpdate");
        performer.setPrintDepartmentId(1);
        performer.setReportDepartmentName("MyReportDepartmentNameUpdate");
		formPerformerDao.save(1,false, performer);
		performer = formPerformerDao.get(1);
		assertEquals("MyNameUpdate", performer.getName());
		assertEquals("MyPhoneUpdate", performer.getPhone());
        assertEquals(Integer.valueOf(1), performer.getPrintDepartmentId());
        assertEquals("MyReportDepartmentNameUpdate", performer.getReportDepartmentName());
	}

	@Test
	public void saveInsertTest() {
		FormDataPerformer performer = new FormDataPerformer();
		performer.setName("MyNameInsert");
		performer.setPhone("MyPhoneInsert");
        performer.setPrintDepartmentId(1);
        performer.setReportDepartmentName("MyReportDepartmentNameInsert");
		assertNull(formPerformerDao.get(2));
		formPerformerDao.save(2, false, performer);
		assertEquals("MyNameInsert", performer.getName());
		assertEquals("MyPhoneInsert", performer.getPhone());
        assertEquals(Integer.valueOf(1), performer.getPrintDepartmentId());
        assertEquals("MyReportDepartmentNameInsert", performer.getReportDepartmentName());


	}

	@Test
	public void clearTest() {
		assertNotNull(formPerformerDao.get(1));
		formPerformerDao.clear(1);
		assertNull(formPerformerDao.get(1));
	}

}
