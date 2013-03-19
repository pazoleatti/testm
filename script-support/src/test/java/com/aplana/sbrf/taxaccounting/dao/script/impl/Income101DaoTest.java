package com.aplana.sbrf.taxaccounting.dao.script.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.script.Income101Dao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"Income101DaoTest.xml"})
public class Income101DaoTest {
	@Autowired
	private Income101Dao income101Dao;

	@Test
	public void testSimple(){
		assertNotNull(income101Dao);
	}

	@Test
	public void testValid(){
		assertEquals(income101Dao.getIncome101(1, "2", 1).getIncomeDebetRemains(), 3, 1e-5);
	}

	@Test
	public void testNotFound(){
		assertNull(income101Dao.getIncome101(1, "not exists", 1));
	}
}


