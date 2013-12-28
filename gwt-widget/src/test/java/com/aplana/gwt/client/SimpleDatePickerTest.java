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
	public void testEnabled(){
		SimpleDatePicker datePicker = new SimpleDatePicker();
		assertTrue(datePicker.isEnabled());
		datePicker.setEnabled(false);
		assertFalse(datePicker.isEnabled());
		datePicker.setEnabled(true);
		assertTrue(datePicker.isEnabled());
	}

	public void testSetValue(){
		SimpleDatePicker datePicker = new SimpleDatePicker();
		Date date = createTestDate();
		datePicker.setValue(date);
		assertTrue(compareDates(date, datePicker.getValue()));
	}

	private Date createTestDate() {
		return new Date(2014, 0, 1);
	}

	public void testHandler(){
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

	private boolean compareDates(Date date1, Date date2){
		DateTimeFormat format = DateTimeFormat.getFormat("d.m.y");
		return format.format(date1).equals(format.format(date2));
	}

	@Override
	public String getModuleName() {
		return "com.aplana.gwt.GwtWidget";
	}
}
