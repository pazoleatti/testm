package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateColumnPresenter;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.ColumnAttributeEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.HashMap;
import java.util.List;


public class FormTemplateColumnView extends ViewWithUiHandlers<FormTemplateColumnUiHandlers>
		implements FormTemplateColumnPresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateColumnView> { }

	private final Widget widget;
	private List<Column> columns;
	private static final HashMap<Integer, String> columnTypeNameMaps = new HashMap<Integer, String>();
	private static final int STRING_TYPE = 0;
	private static final int NUMERIC_TYPE = 1;
	private static final int DATE_TYPE = 2;

	static {
		columnTypeNameMaps.put(STRING_TYPE, "Строка");
		columnTypeNameMaps.put(NUMERIC_TYPE, "Число");
		columnTypeNameMaps.put(DATE_TYPE, "Дата");
	}

	@UiField
	ListBox columnListBox;

	@UiField
	Button upColumn;

	@UiField
	Button downColumn;

	@UiField
	Button addColumn;

	@UiField
	Button removeColumn;

	@UiField
	ColumnAttributeEditor columnAttributeEditor;

	@UiField(provided = true)
	ValueListBox<Integer> typeColumnDropBox;

	@UiField
	TextBox dictionaryCodeBox;

	@UiField
	IntegerBox precisionBox;

	@UiField
	HorizontalPanel precisionPanel;

	@UiField
	HorizontalPanel dictionaryCodePanel;

	@Inject
	@UiConstructor
	public FormTemplateColumnView(Binder uiBinder) {
		typeColumnDropBox = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return columnTypeNameMaps.get(object);
			}
		});

		typeColumnDropBox.addHandler(new ValueChangeHandler<Integer>() {

			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				createNewColumnType();
				selectColumn();
			}
		}, ValueChangeEvent.getType());

		widget = uiBinder.createAndBindUi(this);
	}

	@UiHandler("columnListBox")
	public void onSelectColumn(ChangeEvent event){
		selectColumn();
		setUniqueParameters();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiHandler("precisionBox")
	public void onPrecisionKeyUp(KeyUpEvent event){
		((NumericColumn)columns.get(columnListBox.getSelectedIndex())).setPrecision(precisionBox.getValue());
	}

	@UiHandler("dictionaryCodeBox")
	public void onDictionaryCodeKeyUp(KeyUpEvent event){
		if (typeColumnDropBox.getValue() == STRING_TYPE) {
			((StringColumn)columns.get(columnListBox.getSelectedIndex())).setDictionaryCode(dictionaryCodeBox.getValue());
		}
		if (typeColumnDropBox.getValue() == NUMERIC_TYPE) {
			((NumericColumn)columns.get(columnListBox.getSelectedIndex())).setDictionaryCode(dictionaryCodeBox.getValue());
		}
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
		columnListBox.setSelectedIndex(0);
		selectColumn();
		setUniqueParameters();
	}

	private void setUniqueParameters() {
		Column column = columns.get(columnListBox.getSelectedIndex());

		if (column instanceof StringColumn) {
			typeColumnDropBox.setValue(STRING_TYPE);
		}
        else if (column instanceof NumericColumn) {
			typeColumnDropBox.setValue(NUMERIC_TYPE);
		}
		else {
			typeColumnDropBox.setValue(DATE_TYPE);
		}
		populateUniqueParameters();
		typeColumnDropBox.setAcceptableValues(columnTypeNameMaps.keySet());
	}

	private void populateUniqueParameters() {
		precisionPanel.setVisible(false);
		dictionaryCodePanel.setVisible(false);
		dictionaryCodeBox.setValue(null);
		precisionBox.setValue(null);
		Column column = columns.get(columnListBox.getSelectedIndex());

		if (typeColumnDropBox.getValue() == STRING_TYPE) {
			dictionaryCodeBox.setValue(((StringColumn) column).getDictionaryCode());
			dictionaryCodePanel.setVisible(true);
		}
		else if (typeColumnDropBox.getValue() == NUMERIC_TYPE) {
			dictionaryCodeBox.setValue(((NumericColumn) columns.get(columnListBox.getSelectedIndex())).getDictionaryCode());
			precisionBox.setValue(((NumericColumn) columns.get(columnListBox.getSelectedIndex())).getPrecision());
			precisionPanel.setVisible(true);
			dictionaryCodePanel.setVisible(true);
		}
	}

	private void setColumnList() {
		if (columns != null) {
			columnListBox.clear();
			for (Column column : columns) {
				columnListBox.addItem(column.getName(), String.valueOf(columns.indexOf(column)));
			}
		}
	}

	/**
	 * Поднимает колонку в очереди на 1 позицию.
	 *
	 */
	public void upColumn() {
		int ind = columnListBox.getSelectedIndex();
		Column column = columns.get(ind);
		columnAttributeEditor.flush();

		if (column != null) {
			if (ind > 0) {
				Column exchange = columns.get(ind - 1);
				column.setOrder(ind);
				columns.set(ind - 1, column);
				columns.set(ind, exchange);
				setColumnList();
				columnListBox.setSelectedIndex(ind - 1);
			}
		}
	}

	/**
	 * Опускает колонку в очереди на 1 позицию.
	 *
	 */
	public void downColumn() {
		int ind = columnListBox.getSelectedIndex();
		Column column = columns.get(ind);
		columnAttributeEditor.flush();

		if (column != null) {
			if (ind < columns.size() - 1) {
				Column exchange = columns.get(ind + 1);
				column.setOrder(ind + 2);
				columns.set(ind + 1, column);
				columns.set(ind, exchange);
				setColumnList();
				columnListBox.setSelectedIndex(ind + 1);
			}
		}
	}

	@Override
	public void doFlush() {
		columnAttributeEditor.flush();
	}

	private void selectColumn() {
		columnAttributeEditor.flush();
		columnAttributeEditor.setValue(columns.get(columnListBox.getSelectedIndex()));
	}

	private void createNewColumnType() {
		int index = columnListBox.getSelectedIndex();
		Column column = columns.get(index);
		columnAttributeEditor.flush();

		if (typeColumnDropBox.getValue() == STRING_TYPE) {
			StringColumn stringColumn = new StringColumn();
			copyMainAttribute(column, stringColumn);

			if (dictionaryCodeBox.getValue() != null) {
				stringColumn.setDictionaryCode(dictionaryCodeBox.getText());
			}
			columns.set(index, stringColumn);
		}
		else if (typeColumnDropBox.getValue() == NUMERIC_TYPE) {
			NumericColumn numericColumn = new NumericColumn();
			copyMainAttribute(column, numericColumn);

			if (dictionaryCodeBox.getValue() != null) {
				numericColumn.setDictionaryCode(dictionaryCodeBox.getText());
			}
			if (precisionBox.getValue() != null) {
				numericColumn.setPrecision(precisionBox.getValue());
			}
			columns.set(index, numericColumn);
		}
		else {
			DateColumn dateColumn = new DateColumn();
			copyMainAttribute(column, dateColumn);
			columns.set(index, dateColumn);
		}

		populateUniqueParameters();
	}

	private void copyMainAttribute(Column from, Column to) {
		to.setId(from.getId());
		to.setName(from.getName());
		to.setAlias(from.getAlias());
		to.setGroupName(from.getGroupName());
		to.setWidth(from.getWidth());
		to.setEditable(from.isEditable());
		to.setMandatory(from.isMandatory());
		to.setOrder(from.getOrder());
	}

}