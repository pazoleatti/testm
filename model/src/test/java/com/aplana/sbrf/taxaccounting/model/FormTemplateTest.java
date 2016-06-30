package com.aplana.sbrf.taxaccounting.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class FormTemplateTest {

	private FormTemplate createFormTemplateWithStyles() {
		FormTemplate ft = new FormTemplate();

		FormStyle s = new FormStyle();
		s.setAlias("alias1");
		s.setId(1);
		ft.getStyles().add(s);

		s = new FormStyle();
		s.setAlias("alias2");
		s.setId(2);
		ft.getStyles().add(s);

		s = new FormStyle();
		s.setAlias("alias3");
		s.setId(3);
		ft.getStyles().add(s);

		return ft;
	}
	
	@Test
	public void testGetStyle() {
		FormTemplate ft = createFormTemplateWithStyles();

		FormStyle s = ft.getStyle("alias3");
		assertEquals(new Integer(3), s.getId());

		s = ft.getStyle("alias2");
		assertEquals(new Integer(2), s.getId());

		s = ft.getStyle("alias1");
		assertEquals(new Integer(1), s.getId());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetStyleNullAlias() {
		FormTemplate ft = createFormTemplateWithStyles();
		ft.getStyle(null);

	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetStyleWrongAlias() {
		FormTemplate ft = createFormTemplateWithStyles();
		ft.getStyle("nonExistantAlias");

	}
}
