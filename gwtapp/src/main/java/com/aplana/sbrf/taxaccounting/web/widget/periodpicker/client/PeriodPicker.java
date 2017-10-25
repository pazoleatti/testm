package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.ReportPeriodViewModel;
import com.google.gwt.user.client.ui.HasConstrainedValue;

public interface PeriodPicker extends HasConstrainedValue<List<Integer>>{
	
	void setPeriods(List<ReportPeriodViewModel> periods);
	
	void setTaxType(String taxType);
}
