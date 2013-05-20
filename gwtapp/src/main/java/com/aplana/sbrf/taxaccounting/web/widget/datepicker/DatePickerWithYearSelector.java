package com.aplana.sbrf.taxaccounting.web.widget.datepicker;

import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.datepicker.client.DefaultCalendarView;

public class DatePickerWithYearSelector extends DatePicker {

	public DatePickerWithYearSelector() {
		super(new MonthAndYearSelector(), new DefaultCalendarView(), new
				CalendarModelRussian()) ;
		MonthAndYearSelector monthSelector = (MonthAndYearSelector)
				this.getMonthSelector() ;
		monthSelector.setPicker(this) ;
		monthSelector.setModel(this.getModel()) ;
	}

	public void refreshComponents() {
		super.refreshAll() ;
	}

}