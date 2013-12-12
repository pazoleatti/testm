package com.aplana.gwt.client;


import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Тесты для спиннера
 *
 * @author Vitaliy Samolovskikh
 */
public class SpinnerTest extends GWTTestCase {
	public void testEnabled(){
		Spinner spinner = new Spinner();
		assertTrue(spinner.isEnabled());
		spinner.setEnabled(false);
		assertFalse(spinner.isEnabled());
		spinner.setEnabled(true);
		assertTrue(spinner.isEnabled());
	}

	public void testSetValue(){
		Spinner spinner = new Spinner();
		assertEquals(Integer.valueOf(0), spinner.getValue());

		spinner.setValue(Integer.MIN_VALUE);
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), spinner.getValue());

		spinner.setValue(0);
		assertEquals(Integer.valueOf(0), spinner.getValue());

		spinner.setValue(Integer.MAX_VALUE);
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), spinner.getValue());
	}

	public void testMinMax(){
		Spinner spinner = new Spinner();

		spinner.setMinValue(-100);
		spinner.setMaxValue(100);

		spinner.setValue(-1000);
		assertEquals(Integer.valueOf(-100), spinner.getValue());

		spinner.setValue(0);
		assertEquals(Integer.valueOf(0), spinner.getValue());

		spinner.setValue(1000);
		assertEquals(Integer.valueOf(100), spinner.getValue());
	}

	public void testHandler(){
		final boolean[] flag = {false};

		Spinner spinner = new Spinner();
		spinner.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				flag[0] = true;
			}
		});
		spinner.setValue(1, true);

		assertTrue("Handler didn't work.", flag[0]);
	}

	@Override
	public String getModuleName() {
		return "com.aplana.gwt.GwtWidget";
	}
}
