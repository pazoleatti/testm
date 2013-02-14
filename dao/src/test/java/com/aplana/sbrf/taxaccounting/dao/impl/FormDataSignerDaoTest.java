package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataSignerDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataSignerDaoTest.xml"})
@Transactional
public class FormDataSignerDaoTest {

	@Autowired
	private FormDataSignerDao formDataSignerDao;

	@Autowired
	private FormDataDao formDataDao;

	@Test
	public void getSignersTest() {
		List<FormDataSigner> signers = formDataSignerDao.getSigners(1);
		assertEquals(2, signers.size());
		assertEquals(1, signers.get(0).getId());
		assertEquals("name1", signers.get(0).getName());
		assertEquals("position1", signers.get(0).getPosition());
		assertEquals("name2", signers.get(1).getName());
		assertEquals("position2", signers.get(1).getPosition());
	}

	@Test
	public void saveSignersTest() {
		FormData formData = formDataDao.get(2);
		List<FormDataSigner> formSigners = formData.getSigners();
		for(int i=0; i<2; i++) {
			FormDataSigner newSigner = new FormDataSigner();
			newSigner.setPosition("newPosition"+i);
			newSigner.setName("newName"+i);
			formSigners.add(newSigner);
		}
//		formData.setSigners(formSigners);
		formDataSignerDao.saveSigners(2, formSigners);
		formSigners = formDataSignerDao.getSigners(2);
		assertEquals(4, formSigners.size());
		// Check order
		assertEquals("name3", formSigners.get(0).getName());
		assertEquals("position3", formSigners.get(0).getPosition());
		assertEquals("name4", formSigners.get(1).getName());
		assertEquals("position4", formSigners.get(1).getPosition());
		assertEquals("newName0", formSigners.get(2).getName());
		assertEquals("newPosition0", formSigners.get(2).getPosition());
		assertEquals("newName1", formSigners.get(3).getName());
		assertEquals("newPosition1", formSigners.get(3).getPosition());
	}
}
