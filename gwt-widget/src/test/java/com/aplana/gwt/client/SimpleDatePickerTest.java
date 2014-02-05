package com.aplana.gwt.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.junit.client.GWTTestCase;

import java.util.Date;

/**
 * Тесты для элемента ввода даты.
 *
 * @author Vitaliy Samolovskikh
 */
public class SimpleDatePickerTest extends GWTTestCase {
	public void testEnabled() {
		SimpleDatePicker datePicker = new SimpleDatePicker();
		assertTrue(datePicker.isEnabled());
		datePicker.setEnabled(false);
		assertFalse(datePicker.isEnabled());
		datePicker.setEnabled(true);
		assertTrue(datePicker.isEnabled());
	}

	public void testSetValue() {
		SimpleDatePicker datePicker = new SimpleDatePicker();
		Date date = createTestDate();
		datePicker.setValue(date);
		assertTrue(compareDates(date, datePicker.getValue()));
	}

	public void testEdit() {
		SimpleDatePicker datePicker = new SimpleDatePicker();
		Date date = createTestDate();
		datePicker.getDate().setValue(date.getDate(), true);
		datePicker.getMonth().setSelectedIndex(date.getMonth());
		datePicker.getYear().setValue(date.getYear() + 1900, true);
		assertTrue(compareDates(date, datePicker.getValue()));
	}

	private Date createTestDate() {
		return new Date(2014 - 1900, 0, 1);
	}

	public void testHandler() {
		final boolean[] flag = {false};

		SimpleDatePicker datePicker = new SimpleDatePicker();
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				flag[0] = true;
			}
		});
		datePicker.setValue(createTestDate(), true);

		assertTrue("Handler didn't work.", flag[0]);
	}

	private boolean compareDates(Date date1, Date date2) {
		DateTimeFormat format = DateTimeFormat.getFormat("d.m.y");
		String expected = format.format(date1);
		String actual = format.format(date2);
		if (expected.equals(actual)) {
			return true;
		} else {
			System.out.println("Expected: " + expected + "; actual: " + actual);
			return false;
		}
	}

	@Override
	public String getModuleName() {
		return "com.aplana.gwt.GwtWidget";
	}
}
