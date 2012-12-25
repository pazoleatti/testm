package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateColumnPresenter;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;


public class FormTemplateColumnView extends ViewWithUiHandlers<FormTemplateColumnUiHandlers> implements FormTemplateColumnPresenter.MyView {
	public interface Binder extends UiBinder<Widget, FormTemplateColumnView> { }

	private final Widget widget;
	private List<Column> columns;

	@UiField
	ListBox columnListBox;

	@UiField
	ListBox columnAttributeListBox;

	@UiField
	Button upColumn;

	@UiField
	Button downColumn;

	@UiField
	Button addColumn;

	@UiField
	Button removeColumn;

	@Inject
	public FormTemplateColumnView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@UiHandler("columnListBox")
	public void onSelectColumn(ChangeEvent event){
		selectColumn();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiHandler("upColumn")
	public void onUpCollumn(ClickEvent event){
		upColumn();
	}

	@UiHandler("downColumn")
	public void onDownCollumn(ClickEvent event){
		downColumn();
	}

	@UiHandler("addColumn")
	public void onAddCollumn(ClickEvent event){
		getUiHandlers().addColumn();
	}

	@UiHandler("removeColumn")
	public void onRemoveCollumn(ClickEvent event){
		getUiHandlers().removeColumn(columnListBox.getSelectedIndex());
	}

	@Override
	public void setColumnList(List<Column> columnList) {
		columns = columnList;
		setColumnList();
	}

	private void setColumnList() {
		if (columns != null) {
			columnListBox.clear();
			for (Column column : columns) {
				columnListBox.addItem(column.getName(), String.valueOf(columns.indexOf(column)));
			}
		}
	}

	private void selectColumn() {
		Column column = columns.get(columnListBox.getSelectedIndex());

		columnAttributeListBox.clear();
		columnAttributeListBox.addItem("Имя:" + column.getName());
		columnAttributeListBox.addItem("Имя группы: " + column.getGroupName());
		columnAttributeListBox.addItem("Псевдоним: " + column.getAlias());
	}

	/**
	 * Поднимает колонку в очереди на 1 позицию.
	 *
	 */
	public void upColumn() {
		int ind = columnListBox.getSelectedIndex();
		Column column = columns.get(ind);

		if (column != null) {
			if (ind > 0) {
				Column exchange = columns.get(ind - 1);
				columns.set(ind - 1, column);
				columns.set(ind, exchange);
				setColumnList();
				columnListBox.setSelectedIndex(ind - 1);
			}
		}
	}

	/**
	 * Поднимает колонку в очереди на 1 позицию.
	 *
	 */
	public void downColumn() {
		int ind = columnListBox.getSelectedIndex();
		Column column = columns.get(ind);

		if (column != null) {
			if (ind < columns.size() - 1) {
				Column exchange = columns.get(ind + 1);
				columns.set(ind + 1, column);
				columns.set(ind, exchange);
				setColumnList();
				columnListBox.setSelectedIndex(ind + 1);
			}
		}
	}

}