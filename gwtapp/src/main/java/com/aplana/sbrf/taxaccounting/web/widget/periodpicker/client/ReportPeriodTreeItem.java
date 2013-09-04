package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TreeItem;

public class ReportPeriodTreeItem extends TreeItem implements HasValue<Boolean>{
	
	private static final String RADIO_BUTTON_GROUP  = "PERIOD_GROUP";
	
	private Integer id;
	
	public ReportPeriodTreeItem(ReportPeriod reportPeriod, boolean multiselect){
		this.id = reportPeriod.getId();
		CheckBox checkBox;
		if(multiselect){
			checkBox = new CheckBox(reportPeriod.getName());
		} else {
			checkBox = new RadioButton(RADIO_BUTTON_GROUP, reportPeriod.getName());
		}
		setWidget(checkBox);
		
	}

	public Integer getId() {
		return id;
	}
	
	public void setValue(Boolean value){
		((CheckBox) getWidget()).setValue(value);
	}
	
	public Boolean getValue(){
		return ((CheckBox) getWidget()).getValue();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Boolean> handler) {
		return ((CheckBox) getWidget()).addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		((CheckBox) getWidget()).fireEvent(event);
	}

	@Override
	public void setValue(Boolean value, boolean fireEvents) {
		((CheckBox) getWidget()).setValue(value, fireEvents);
	}

}
