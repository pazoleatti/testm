package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.StyleDao;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import static org.junit.Assert.assertEquals;

import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.03.2016 14:28
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"StyleTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StyleDaoImplTest {

	@Autowired
	private StyleDao dao;

	@Test
	public void getAllTest() {
		List<FormStyle> styles = dao.getAll();
		assertEquals(7, styles.size());
		assertEquals("Корректировка-без изменений", styles.get(2).getAlias());
	}

	@Test
	public void getTest() {
		FormStyle style = dao.get("Редактируемая");
		assertEquals(Color.LIGHT_BLUE, style.getBackColor());
	}

	@Test(expected = DaoException.class)
	public void getTest2() {
		FormStyle style = dao.get("something");
	}

}