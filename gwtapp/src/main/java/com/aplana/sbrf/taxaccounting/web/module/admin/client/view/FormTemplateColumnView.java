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
	private static final HashMap<Integer, String> columnTypeNameMap = new HashMap<Integer, String>();
	private static final HashMap<Integer, String> precisionMap = new HashMap<Integer, String>();
	private static final HashMap<String, String> stringDictionaryCodeMap = new HashMap<String, String>();
	private static final HashMap<String, String> numericDictionaryCodeMap = new HashMap<String, String>();
	private static final int STRING_TYPE = 0;
	private static final int NUMERIC_TYPE = 1;
	private static final int DATE_TYPE = 2;

	static {
		columnTypeNameMap.put(STRING_TYPE, "Строка");
		columnTypeNameMap.put(NUMERIC_TYPE, "Число");
		columnTypeNameMap.put(DATE_TYPE, "Дата");

		stringDictionaryCodeMap.put("transportTypeCode", "ТН - Коды видов ТС");
		stringDictionaryCodeMap.put("transportOkato", "ТН - Коды ОКАТО");
		stringDictionaryCodeMap.put("transportTaxBenefitCode", "ТН - Коды налоговых льгот");

		numericDictionaryCodeMap.put("transportTaxBaseUnitCode", "ТН - Коды единиц измерения");
		numericDictionaryCodeMap.put("transportEcoClass", "ТН - Коды классов экологических стандартов");

		precisionMap.put(0, "0");
		precisionMap.put(1, "1");
		precisionMap.put(2, "2");
		precisionMap.put(3, "3");
		precisionMap.put(4, "4");
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

	@UiField(provided = true)
	ValueListBox<String> stringDictionaryCodeBox;

	@UiField(provided = true)
	ValueListBox<String> numericDictionaryCodeBox;

	@UiField
	TextBox nameBox;

	@UiField(provided = true)
	ValueListBox<Integer> precisionBox;

	@UiField
	HorizontalPanel precisionPanel;

	@UiField
	HorizontalPanel dictionaryCodePanel;

	@Inject
	@UiConstructor
	public FormTemplateColumnView(Binder uiBinder) {
		init();
		widget = uiBinder.createAndBindUi(this);
	}

	private void init() {
		typeColumnDropBox = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return columnTypeNameMap.get(object);
			}
		});
		typeColumnDropBox.addHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				createNewColumnType();
				setColumnAttributeEditor(columnListBox.getSelectedIndex());
			}
		}, ValueChangeEvent.getType());

		precisionBox = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				return precisionMap.get(object);
			}
		});

		precisionBox.addHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				precisionPanel.setVisible(true);
				((NumericColumn)columns.get(columnListBox.getSelectedIndex())).setPrecision(precisionBox.getValue());
			}
		}, ValueChangeEvent.getType());

		stringDictionaryCodeBox = new ValueListBox<String>(new AbstractRenderer<String>() {
			@Override
			public String render(String object) {
				return stringDictionaryCodeMap.get(object);
			}
		});
		stringDictionaryCodeBox.setAcceptableValues(stringDictionaryCodeMap.keySet());

		stringDictionaryCodeBox.addHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				((StringColumn)columns.get(columnListBox.getSelectedIndex())).setDictionaryCode(stringDictionaryCodeBox.getValue());
			}
		}, ValueChangeEvent.getType());

		numericDictionaryCodeBox = new ValueListBox<String>(new AbstractRenderer<String>() {
			@Override
			public String render(String object) {
				return numericDictionaryCodeMap.get(object);
			}
		});
		numericDictionaryCodeBox.setAcceptableValues(numericDictionaryCodeMap.keySet());

		numericDictionaryCodeBox.addHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				((NumericColumn)columns.get(columnListBox.getSelectedIndex())).setDictionaryCode(numericDictionaryCodeBox.getValue());
			}
		}, ValueChangeEvent.getType());
	}

	@UiHandler("columnListBox")
	public void onSelectColumn(ChangeEvent event){
		setColumnAttributeEditor(columnListBox.getSelectedIndex());
		setUniqueParameters();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiHandler("upColumn")
	public void onUpColumn(ClickEvent event){
		int ind = columnListBox.getSelectedIndex();
		Column column = columns.get(ind);
		flush();

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

	@UiHandler("downColumn")
	public void onDownColumn(ClickEvent event){
		int ind = columnListBox.getSelectedIndex();
		Column column = columns.get(ind);
		flush();

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

	@UiHandler("nameBox")
	public void onNameTextBoxKeyPressed(KeyUpEvent event){
		changeNameColumn();
	}
	@UiHandler("nameBox")
	public void onNameTextBoxClicked(ClickEvent event){
		changeNameColumn();
	}
	private void changeNameColumn() {
		int index = columnListBox.getSelectedIndex();
		Column column = columns.get(index);
		column.setName(nameBox.getValue());
		setupColumns(index);
	}

	@UiHandler("addColumn")
	public void onAddColumn(ClickEvent event){
		Column newColumn = new StringColumn();
		newColumn.setName("Новый столбец");
		newColumn.setAlias("псевдоним");
		newColumn.setWidth(5);
		newColumn.setEditable(true);
		newColumn.setOrder(columns.size() + 1);
		columns.add(newColumn);
		setupColumns(columns.size() - 1);
		getUiHandlers().addColumn(newColumn);
	}

	@UiHandler("removeColumn")
	public void onRemoveColumn(ClickEvent event){
		int index = columnListBox.getSelectedIndex();
		getUiHandlers().removeColumn(columns.get(index));
		columns.remove(index);
		if (index > 0) {
			setupColumns(index-1);
		}
		else {
			setupColumns(0);
		}
	}

	@Override
	public void setColumnList(List<Column> columnList) {
		columns = columnList;
		setupColumns(0);
		setColumnAttributeEditor(0);
	}

	@Override
	public void flush() {
		columnAttributeEditor.flush();
	}

	private void setupColumns(int index) {
		setColumnList();
		setColumnAttributeEditor(index);
		columnListBox.setSelectedIndex(index);
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
		typeColumnDropBox.setAcceptableValues(columnTypeNameMap.keySet());
	}

	private void populateUniqueParameters() {
		Column column = columns.get(columnListBox.getSelectedIndex());
		precisionPanel.setVisible(false);
		dictionaryCodePanel.setVisible(false);
		stringDictionaryCodeBox.setVisible(false);
		numericDictionaryCodeBox.setVisible(false);
		nameBox.setValue(column.getName());

		if (typeColumnDropBox.getValue() == STRING_TYPE) {
			String code = ((StringColumn) column).getDictionaryCode();
			if (stringDictionaryCodeMap.containsKey(code)) {
				stringDictionaryCodeBox.setValue(code);
			}
			else {
				stringDictionaryCodeBox.setValue(null);
			}

			stringDictionaryCodeBox.setVisible(true);
			dictionaryCodePanel.setVisible(true);
		}
		else if (typeColumnDropBox.getValue() == NUMERIC_TYPE) {
			String code = ((NumericColumn) column).getDictionaryCode();
			if (numericDictionaryCodeMap.containsKey(code)) {
				numericDictionaryCodeBox.setValue(code);
			}
			else {
				numericDictionaryCodeBox.setValue(null);
			}

			numericDictionaryCodeBox.setVisible(true);
			precisionPanel.setVisible(true);
			precisionBox.setValue(((NumericColumn) column).getPrecision());
			precisionBox.setAcceptableValues(precisionMap.keySet());
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

	private void setColumnAttributeEditor(int index) {
		flush();
		columnAttributeEditor.setValue(columns.get(index));
	}

	private void createNewColumnType() {
		int index = columnListBox.getSelectedIndex();
		Column column = columns.get(index);
		flush();

		if (typeColumnDropBox.getValue() == STRING_TYPE) {
			StringColumn stringColumn = new StringColumn();
			copyMainColumnAttributes(column, stringColumn);
			columns.set(index, stringColumn);

			getUiHandlers().removeColumn(column);
			getUiHandlers().addColumn(stringColumn);
		}
		else if (typeColumnDropBox.getValue() == NUMERIC_TYPE) {
			NumericColumn numericColumn = new NumericColumn();
			copyMainColumnAttributes(column, numericColumn);
			columns.set(index, numericColumn);

			getUiHandlers().removeColumn(column);
			getUiHandlers().addColumn(numericColumn);
		}
		else {
			DateColumn dateColumn = new DateColumn();
			copyMainColumnAttributes(column, dateColumn);
			columns.set(index, dateColumn);

			getUiHandlers().removeColumn(column);
			getUiHandlers().addColumn(dateColumn);
		}

		populateUniqueParameters();
	}

	private void copyMainColumnAttributes(Column from, Column to) {
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