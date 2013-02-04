package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DepartmentFormTypeDaoTest.xml" })
@Transactional
public class DepartmentFormTypeDaoTest {
	
	@Autowired
	DepartmentFormTypeDao departmentFormTypeDao;
	
	@Test
	public void getSources(){
		assertEquals(5, departmentFormTypeDao.getSources(2, 1, FormDataKind.fromId(3)).size());
	}
	
	@Test
	public void getDestanations(){
		assertEquals(2, departmentFormTypeDao.getDestanations(1, 1, FormDataKind.fromId(3)).size());
	}
	
	@Test
	public void getSourceDepartmentIds(){
		assertEquals(new HashSet<Integer>(Arrays.asList(new Integer[]{3, 1})), new HashSet<Integer>(departmentFormTypeDao.getSourceDepartmentIds(2, TaxType.fromCode('T'))));
	}

}
