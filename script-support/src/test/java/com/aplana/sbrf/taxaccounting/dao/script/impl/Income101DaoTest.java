package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.script.Income101Dao;
import com.aplana.sbrf.taxaccounting.model.Income101;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;
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
        List<Income101> income101List = income101Dao.getIncome101(1, "2");
		assertEquals(income101List.get(0).getIncomeDebetRemains(), 3, 1e-5);
	}

	@Test
	public void testNotFound(){
		assertTrue(income101Dao.getIncome101(1, "not exists").size() == 0);
	}
}


