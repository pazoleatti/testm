package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateColumnPresenter;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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

	@Override
	public void setColumnList(List<Column> columnList) {
		columns = columnList;
		if (columnList != null) {
			columnListBox.clear();
			for (Column column : columnList) {
				columnListBox.addItem(column.getName(), String.valueOf(columnList.indexOf(column)));
			}
		}
	}

	private void selectColumn() {
		Column column = columns.get(columnListBox.getSelectedIndex());

		columnAttributeListBox.clear();
		columnAttributeListBox.addItem("Имя группы: " + column.getGroupName());
		columnAttributeListBox.addItem("Имя:" + column.getName());
		columnAttributeListBox.addItem("Псевдоним: " + column.getAlias());
	}

}