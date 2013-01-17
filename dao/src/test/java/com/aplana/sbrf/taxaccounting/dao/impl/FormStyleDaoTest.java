package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormTemplateDaoTest.xml"})
public class FormStyleDaoTest {

	@Autowired
	private FormStyleDao formStyleDao;

	private static final int FORM_ID_FOR_TEST = 1;
	private static final int NUMBER_OF_STYLES = 3;
	private static final int FIRST_STYLE  = 0;
	private static final int SECOND_STYLE = 1;
	private static final int THIRD_STYLE  = 2;

	@Test
	public void getFormStyles(){
	    //Given FORM_ID_FOR_TEST, NUMBER_OF_STYLES

		//When
		List<FormStyle> formStyleList = formStyleDao.getFormStyles(FORM_ID_FOR_TEST);

		//Then
		Assert.assertEquals(NUMBER_OF_STYLES, formStyleList.size());

		Assert.assertEquals(Integer.valueOf(1), formStyleList.get(FIRST_STYLE).getId());
		Assert.assertEquals("alias1", formStyleList.get(FIRST_STYLE).getAlias());
		Assert.assertEquals(Color.BLUE, formStyleList.get(FIRST_STYLE).getFontColor());
		Assert.assertEquals(Color.GREEN, formStyleList.get(FIRST_STYLE).getBackColor());
		Assert.assertEquals(true, formStyleList.get(FIRST_STYLE).isItalic());
		Assert.assertEquals(false, formStyleList.get(FIRST_STYLE).isBold());

		Assert.assertEquals(Integer.valueOf(2), formStyleList.get(SECOND_STYLE).getId());
		Assert.assertEquals("alias2", formStyleList.get(SECOND_STYLE).getAlias());
		Assert.assertEquals(Color.GREEN, formStyleList.get(SECOND_STYLE).getFontColor());
		Assert.assertEquals(Color.BLUE, formStyleList.get(SECOND_STYLE).getBackColor());
		Assert.assertEquals(false, formStyleList.get(SECOND_STYLE).isItalic());
		Assert.assertEquals(true, formStyleList.get(SECOND_STYLE).isBold());

		Assert.assertEquals(Integer.valueOf(3), formStyleList.get(THIRD_STYLE).getId());
		Assert.assertEquals("alias3", formStyleList.get(THIRD_STYLE).getAlias());
		Assert.assertEquals(Color.RED, formStyleList.get(THIRD_STYLE).getFontColor());
		Assert.assertEquals(Color.RED, formStyleList.get(THIRD_STYLE).getBackColor());
		Assert.assertEquals(true, formStyleList.get(THIRD_STYLE).isItalic());
		Assert.assertEquals(true, formStyleList.get(THIRD_STYLE).isBold());
	}
}
