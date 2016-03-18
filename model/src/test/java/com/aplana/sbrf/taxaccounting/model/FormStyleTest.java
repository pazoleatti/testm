package com.aplana.sbrf.taxaccounting.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 18.03.2016 18:05
 */

public class FormStyleTest {

	@Test
	public void getStyleTest() {
		FormStyle style = FormStyle.valueOf("s0-1");
		assertEquals(Color.BLACK, style.getFontColor());
		assertEquals(Color.LIGHT_YELLOW, style.getBackColor());

		style = FormStyle.valueOf("s0-1b");
		assertEquals(Color.BLACK, style.getFontColor());
		assertEquals(Color.LIGHT_YELLOW, style.getBackColor());
		assertFalse(style.isItalic());
		assertTrue(style.isBold());

		style = FormStyle.valueOf("s11-10ib"); // переставил местами
		assertEquals(Color.DARK_BLUE, style.getFontColor());
		assertEquals(Color.RED, style.getBackColor());
		assertTrue(style.isItalic());
		assertTrue(style.isBold());

		style = FormStyle.valueOf("s1i1-b10"); // переставил местами
		assertEquals(Color.DARK_BLUE, style.getFontColor());
		assertEquals(Color.RED, style.getBackColor());
		assertTrue(style.isItalic());
		assertTrue(style.isBold());
	}

	@Test
	public void tudaSudaTest() {
		String styleString = "s0-12b";
		assertEquals(styleString, FormStyle.valueOf(styleString).toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void errorTest() {
		FormStyle.valueOf("t0-12b");
	}

	@Test(expected = IllegalArgumentException.class)
	public void errorTest2() {
		FormStyle.valueOf("sss");
	}

}