package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormPerformerDaoTest.xml"})
@Transactional
public class FormPerformerDaoTest {
	@Autowired
	private FormPerformerDao formPerformerDao;

	@Test
	public void getTest() {
		FormDataPerformer performer = formPerformerDao.get(1);
		assertEquals("name1", performer.getName());
		assertEquals("phone1", performer.getPhone());
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
		formPerformerDao.save(1,performer);
		performer = formPerformerDao.get(1);
		assertEquals("MyNameUpdate", performer.getName());
		assertEquals("MyPhoneUpdate", performer.getPhone());

	}

	@Test
	public void saveInsertTest() {
		FormDataPerformer performer = new FormDataPerformer();
		performer.setName("MyNameInsert");
		performer.setPhone("MyPhoneInsert");
		assertNull(formPerformerDao.get(2));
		formPerformerDao.save(2,performer);
		assertEquals("MyNameInsert", performer.getName());
		assertEquals("MyPhoneInsert", performer.getPhone());


	}

	@Test
	public void clearTest() {
		assertNotNull(formPerformerDao.get(1));
		formPerformerDao.clear(1);
		assertNull(formPerformerDao.get(1));
	}

}
