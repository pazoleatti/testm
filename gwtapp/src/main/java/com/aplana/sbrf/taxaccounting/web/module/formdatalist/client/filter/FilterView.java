package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter.FilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter.SelectItem;
import com.aplana.sbrf.taxaccounting.web.widget.listeditor.client.ListBoxEditor;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class FilterView extends ViewImpl implements FilterPresenter.MyView, Editor<FilterData>{

	interface MyBinder extends UiBinder<Widget, FilterView> {
	}
	
	interface MyDriver extends SimpleBeanEditorDriver<FilterData, FilterView>{
	}
	
	private final Widget widget;
	
	private final MyDriver driver;
	
	@UiField
	ListBoxEditor period;
	
	@UiField
	ListBoxEditor department;
	
	@UiField
	ListBoxEditor formtype;

	@Inject
	public FilterView(final MyBinder binder, final MyDriver driver) {
		widget = binder.createAndBindUi(this);
		this.driver = driver;
		this.driver.initialize(this);
	}


	@Override
	public Widget asWidget() {
		return widget;
	}


	@Override
	public void setFilterData(FilterData filterData) {
		driver.edit(filterData);
	}


	@Override
	public FilterData getFilterData() {
		return driver.flush();
	}


	@Override
	public void setPeriodList(List<SelectItem> list) {
		for (SelectItem selectItem : list) {
			period.addItem(selectItem.getName(), String.valueOf(selectItem.getId()));
		}
		
	}


	@Override
	public void setDepartmentList(List<SelectItem> list) {
		for (SelectItem selectItem : list) {
			department.addItem(selectItem.getName(), String.valueOf(selectItem.getId()));
		}
	}


	@Override
	public void setFormtypeList(List<SelectItem> list) {
		for (SelectItem selectItem : list) {
			formtype.addItem(selectItem.getName(), String.valueOf(selectItem.getId()));
		}
	}

}
