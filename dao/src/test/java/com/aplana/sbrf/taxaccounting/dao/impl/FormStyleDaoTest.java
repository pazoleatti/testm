package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormStyleDaoTest.xml"})
public class FormStyleDaoTest {

	@Autowired
	private FormStyleDao formStyleDao;

	private static final int FORM_ID_FOR_TEST = 1;
	private static final int NUMBER_OF_STYLES = 3;
	private static final int FIRST_STYLE  = 0;
	private static final int SECOND_STYLE = 1;
	private static final int THIRD_STYLE  = 2;

	@Test
	@Transactional
	public void getFormStyles(){
	    //Given FORM_ID_FOR_TEST, NUMBER_OF_STYLES

		//When
		List<FormStyle> listOfStylesInDb = formStyleDao.getFormStyles(FORM_ID_FOR_TEST);

		//Then
		Assert.assertEquals(NUMBER_OF_STYLES, listOfStylesInDb.size());

		Assert.assertEquals(Integer.valueOf(1), listOfStylesInDb.get(FIRST_STYLE).getId());
		Assert.assertEquals("alias1", listOfStylesInDb.get(FIRST_STYLE).getAlias());
		Assert.assertEquals(Color.BLUE, listOfStylesInDb.get(FIRST_STYLE).getFontColor());
		Assert.assertEquals(Color.GREEN, listOfStylesInDb.get(FIRST_STYLE).getBackColor());
		Assert.assertEquals(true, listOfStylesInDb.get(FIRST_STYLE).isItalic());
		Assert.assertEquals(false, listOfStylesInDb.get(FIRST_STYLE).isBold());

		Assert.assertEquals(Integer.valueOf(2), listOfStylesInDb.get(SECOND_STYLE).getId());
		Assert.assertEquals("alias2", listOfStylesInDb.get(SECOND_STYLE).getAlias());
		Assert.assertEquals(Color.GREEN, listOfStylesInDb.get(SECOND_STYLE).getFontColor());
		Assert.assertEquals(Color.BLUE, listOfStylesInDb.get(SECOND_STYLE).getBackColor());
		Assert.assertEquals(false, listOfStylesInDb.get(SECOND_STYLE).isItalic());
		Assert.assertEquals(true, listOfStylesInDb.get(SECOND_STYLE).isBold());

		Assert.assertEquals(Integer.valueOf(3), listOfStylesInDb.get(THIRD_STYLE).getId());
		Assert.assertEquals("alias3", listOfStylesInDb.get(THIRD_STYLE).getAlias());
		Assert.assertEquals(Color.RED, listOfStylesInDb.get(THIRD_STYLE).getFontColor());
		Assert.assertEquals(Color.RED, listOfStylesInDb.get(THIRD_STYLE).getBackColor());
		Assert.assertEquals(true, listOfStylesInDb.get(THIRD_STYLE).isItalic());
		Assert.assertEquals(true, listOfStylesInDb.get(THIRD_STYLE).isBold());
	}

	@Test
	@Transactional
	public void saveFormStyles(){
		//Given FORM_ID_FOR_TEST
		List<FormStyle> formStyleList = formStyleDao.getFormStyles(FORM_ID_FOR_TEST);
		//Создадим один новый стиль, которого нету в БД
		FormStyle newFormsStyle = new FormStyle();
		newFormsStyle.setAlias("newFormStyle");
		newFormsStyle.setFontColor(Color.WHITE);
		newFormsStyle.setBackColor(Color.WHITE);
		newFormsStyle.setItalic(false);
		newFormsStyle.setBold(false);
		formStyleList.add(newFormsStyle);

		FormTemplate formTemplate = new FormTemplate();
		formTemplate.setId(FORM_ID_FOR_TEST);
		formTemplate.getStyles().addAll(formStyleList);

		//When
		formStyleDao.saveFormStyles(formTemplate);

		//Then
		formStyleList = formStyleDao.getFormStyles(FORM_ID_FOR_TEST);
		newFormsStyle = formStyleList.get(3);

		Assert.assertEquals(4, formStyleList.size());
		Assert.assertEquals("newFormStyle", newFormsStyle.getAlias());
		Assert.assertEquals(Color.WHITE, newFormsStyle.getFontColor());
		Assert.assertEquals(Color.WHITE, newFormsStyle.getBackColor());
		Assert.assertEquals(false, newFormsStyle.isItalic());
		Assert.assertEquals(false, newFormsStyle.isBold());
	}
}
