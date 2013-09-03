package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class TaxPeriodTreeItem  extends TreeItem{

	
	public TaxPeriodTreeItem(TaxPeriod taxPeriod) {
		super();
		Widget widget = new Label(getFormattedTaxPeriodDate(taxPeriod));
		setWidget(widget);
	}
	
	private String getFormattedTaxPeriodDate(TaxPeriod taxPeriod){
		final String dateShortStart = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)
				.format(taxPeriod.getStartDate());
		final String dateShortEnd = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)
				.format(taxPeriod.getEndDate());
		int startDayIndex = dateShortStart.lastIndexOf('-');
		int startMonthIndex = dateShortStart.indexOf('-');
		int endDayIndex = dateShortEnd.lastIndexOf('-');
		int enMonthIndex = dateShortEnd.indexOf('-');
		String startDate =  dateShortStart.substring(startDayIndex + 1, dateShortStart.length()) + '.' +
							dateShortStart.substring(startMonthIndex + 1, startDayIndex) + '.' +
							dateShortStart.substring(0, startMonthIndex);
		String endDate =dateShortEnd.substring(endDayIndex + 1, dateShortEnd.length()) + '.' +
						dateShortEnd.substring(enMonthIndex + 1, endDayIndex) + '.' +
						dateShortEnd.substring(0, enMonthIndex);
		return (startDate + " - " + endDate);
	}
	
}
