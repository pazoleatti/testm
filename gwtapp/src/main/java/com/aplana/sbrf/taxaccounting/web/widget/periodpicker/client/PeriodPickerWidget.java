package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class PeriodPickerWidget extends Composite implements PeriodPicker{
	
	interface Binder extends UiBinder<Widget, PeriodPickerWidget> {
	}
	private static Binder binder = GWT.create(Binder.class);
		
	private boolean multiselect;
	
	@UiField
	Tree tree;

	public PeriodPickerWidget(){
		initWidget(binder.createAndBindUi(this));
	}
	
	public PeriodPickerWidget(boolean multiselect){
		this();
		this.multiselect = multiselect;
	}
	
	
	@Override
	public void setAcceptableValues(Collection<List<Integer>> values) {
		// All value acceptable
	}

	@Override
	public List<Integer> getValue() {
		List<Integer> result = new ArrayList<Integer>();
		for (ReportPeriodTreeItem periodItem : getPeriodItem()) {
			if (periodItem.getValue()){
				result.add(periodItem.getId());
			}
		}
		return result;
	}

	@Override
	public void setValue(List<Integer> value) {
		setValue(value, false);
	}

	@Override
	public void setValue(List<Integer> value, boolean fireEvents) {
		for (ReportPeriodTreeItem periodItem : getPeriodItem()) {
			if (value != null && value.contains(periodItem.getId())){
				periodItem.setValue(true);
				periodItem.getParentItem().setState(true);
			} else {
				periodItem.setValue(false);
			}
		}
		if (fireEvents) {
			ValueChangeEvent.fire(this, value);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<List<Integer>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void setPeriods(List<ReportPeriod> periods) {
		tree.clear();
		
		Map<Integer, YearTreeItem> periodYearsMap = new LinkedHashMap<Integer, YearTreeItem>();
		
		for(ReportPeriod reportPeriod : periods){
			
			if (!periodYearsMap.containsKey(reportPeriod.getYear())){
				YearTreeItem taxPeriodItem = new YearTreeItem(reportPeriod.getYear());
				periodYearsMap.put(reportPeriod.getYear(), taxPeriodItem);
			}

			ReportPeriodTreeItem reportPeriodItem = new ReportPeriodTreeItem(reportPeriod, multiselect);
			reportPeriodItem.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					ValueChangeEvent.fire(PeriodPickerWidget.this, getValue());
				}
				
			});
			periodYearsMap.get(reportPeriod.getYear()).addItem(reportPeriodItem);
		}
		
		for (YearTreeItem taxPeriodTreeItem : periodYearsMap.values()) {
			tree.addItem(taxPeriodTreeItem);
		}
		
	}
	
	
	private Collection<ReportPeriodTreeItem> getPeriodItem(){
		Collection<ReportPeriodTreeItem> result = new ArrayList<ReportPeriodTreeItem>();
		for (int i = 0; i < tree.getItemCount(); i++) {
			TreeItem taxItem = tree.getItem(i);
			for (int j = 0; j < taxItem.getChildCount(); j++) {
				ReportPeriodTreeItem periodItem = (ReportPeriodTreeItem) taxItem.getChild(j);
				result.add(periodItem);
			}
		}
		return result;
	}

	@Override
	public void setTaxType(String taxType) {
		// Операция не поддерживается. Пока не нужна была.
		throw new UnsupportedOperationException();
	}


}
