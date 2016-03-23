package com.aplana.sbrf.taxaccounting.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormTemplateTest {

	private FormTemplate createFormTemplate() {
		FormTemplate ft = new FormTemplate();

		ft.getStyles().add(new FormStyle("alias1", Color.DARK_GREEN, Color.WHITE, false, false));
		ft.getStyles().add(new FormStyle("alias2", Color.RED, Color.LIGHT_BLUE, false, false));
		ft.getStyles().add(new FormStyle("alias3", Color.BLUE, Color.LIGHT_CORAL, false, false));

		Column column = new StringColumn();
		column.setId(2);
		column.setAlias("col1");
		ft.getColumns().add(column);
		column = new StringColumn();
		column.setId(6);
		column.setAlias("col2");
		ft.getColumns().add(column);

		return ft;
	}
	
	@Test
	public void getColumnTest() {
		FormTemplate ft = createFormTemplate();
		assertEquals("col1", ft.getColumn("col1").getAlias());
		assertEquals("col2", ft.getColumn("col2").getAlias());
		assertEquals("col2", ft.getColumn(6).getAlias());
		assertEquals("col1", ft.getColumn(2).getAlias());
	}

	@Test(expected = IllegalArgumentException.class)
	public void getColumnTest2() {
		FormTemplate ft = createFormTemplate();
		Integer i = null;
		ft.getColumn(i);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getColumnTest3() {
		FormTemplate ft = createFormTemplate();
		ft.getColumn(1000);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getColumnTest4() {
		FormTemplate ft = createFormTemplate();
		String s = null;
		ft.getColumn(s);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getColumnTest5() {
		FormTemplate ft = createFormTemplate();
		ft.getColumn("something");
	}
}