package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.script.Income102Dao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"Income102DaoTest.xml"})
public class Income102DaoTest {
	@Autowired
	private Income102Dao income102Dao;
	
	@Test
	public void testSimple(){
		assertNotNull(income102Dao);
	}
	
	@Test
	public void testValid(){
		assertEquals(income102Dao.getIncome102(1, "2", 1).getTotalSum(), 666, 1e-5);
	}

	@Test
	public void testNotFound(){
		assertNull(income102Dao.getIncome102(1, "not exists", 1));
	}
}


