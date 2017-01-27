package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataSignerDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore("Налоговые формы не используются!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataSignerDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
        assertEquals(1, signers.get(0).getOrd());
        assertEquals(2, signers.get(1).getId());
        assertEquals("name2", signers.get(1).getName());
		assertEquals("position2", signers.get(1).getPosition());
        assertEquals(2, signers.get(1).getOrd());
    }

	@Test
	public void saveSignersTest() {
		FormData formData = formDataDao.get(2, false);
		List<FormDataSigner> formSigners = formData.getSigners();
		assertEquals(2, formSigners.size());
		for(int i=0; i<2; i++) {
			FormDataSigner newSigner = new FormDataSigner();
			newSigner.setPosition("newPosition"+i);
			newSigner.setName("newName"+i);
			formSigners.add(newSigner);
		}
		formDataSignerDao.saveSigners(2, formSigners);
		formSigners = formDataSignerDao.getSigners(2);
		assertEquals(4, formSigners.size());
		// Check order
		assertEquals("name3", formSigners.get(0).getName());
		assertEquals("position3", formSigners.get(0).getPosition());
        assertEquals(1, formSigners.get(0).getOrd());
		assertEquals("name4", formSigners.get(1).getName());
		assertEquals("position4", formSigners.get(1).getPosition());
        assertEquals(2, formSigners.get(1).getOrd());
		assertEquals("newName0", formSigners.get(2).getName());
		assertEquals("newPosition0", formSigners.get(2).getPosition());
        assertEquals(3, formSigners.get(2).getOrd());
		assertEquals("newName1", formSigners.get(3).getName());
		assertEquals("newPosition1", formSigners.get(3).getPosition());
        assertEquals(4, formSigners.get(3).getOrd());
	}
}
