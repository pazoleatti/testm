package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateColumnPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.ColumnAttributeEditor;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class FormTemplateColumnView extends ViewWithUiHandlers<FormTemplateColumnUiHandlers>
		implements FormTemplateColumnPresenter.MyView {


    public interface Binder extends UiBinder<Widget, FormTemplateColumnView> { }

    // Типы графы
    private static final String STRING_TYPE = "Строка";
    private static final String NUMERIC_TYPE = "Число";
    private static final String DATE_TYPE = "Дата";
    private static final String REFBOOK_TYPE = "Справочник";
    private static final String REFERENCE_TYPE = "Зависимая графа";

	private List<Column> columns;

	private static final List<String> columnTypeNameList = Arrays.asList(STRING_TYPE, NUMERIC_TYPE, DATE_TYPE,
            REFBOOK_TYPE, REFERENCE_TYPE);
	private static final List<Integer> precisionList = new ArrayList<Integer>();
	private static final List<Formats> dateFormatList = new ArrayList<Formats>();

	static {
		for(int i = 0; i <= NumericColumn.MAX_PRECISION; i++) {
			precisionList.add(i);
		}

		for (Formats f : Formats.values()) {
			if ((f != Formats.NONE) && (f != null)) {
				dateFormatList.add(f);
			}
		}
	}

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

	@UiField
	IntegerBox stringMaxLengthBox;

	@UiField
	IntegerBox numericMaxLengthBox;

	@UiField
	TextBox nameBox;

	@UiField
	Panel precisionPanel;

	@UiField
	Panel attrPanel;

	@UiField
	Panel stringMaxLengthPanel;

	@UiField
	Panel numericMaxLengthPanel;

	@UiField
	Panel dateFormatPanel;

    @UiField
    Panel refBookPanel;

    @UiField
    Panel refBookAttrPanel;

    @UiField
    Panel refBookAttrFilterPanel;

    @UiField
    Panel refBooktAttrParentPanel;

    @UiField
    ListBox columnListBox;

	@UiField(provided = true)
	ValueListBox<Formats> dateFormat;

    @UiField(provided = true)
    ValueListBox<String> typeColumnDropBox;

    @UiField(provided = true)
    ValueListBox<Integer> precisionBox;

    @UiField(provided = true)
    ValueListBox<RefBook> refBookBox;

    @UiField(provided = true)
    ValueListBox<RefBookAttribute> refBookAttrBox;

    @UiField(provided = true)
    ValueListBox<RefBookAttribute> refBookAttrRefBox;

    @UiField(provided = true)
    ValueListBox<Column> refBooktAttrParentBox;

    @UiField
    TextArea refBookAttrFilterArea;

    @UiField
    Label errorMsg;

    @Inject
	@UiConstructor
	public FormTemplateColumnView(Binder binder) {
		init();
		initWidget(binder.createAndBindUi(this));
	}

	private void init() {
		typeColumnDropBox = new ValueListBox<String>(new AbstractRenderer<String>() {
			@Override
			public String render(String object) {
				if (object == null) {
					return "";
				}
				return object;
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
				return "" + object;
			}
		});

		precisionBox.addHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				precisionPanel.setVisible(true);
				((NumericColumn)columns.get(columnListBox.getSelectedIndex())).setPrecision(precisionBox.getValue());
			}
		}, ValueChangeEvent.getType());

		dateFormat = new ValueListBox<Formats>(new AbstractRenderer<Formats>() {
			@Override
			public String render(Formats format) {
				return format == null ? "" : format.getFormat();
			}
		});

        dateFormat.addValueChangeHandler(new ValueChangeHandler<Formats>() {
            @Override
            public void onValueChange(ValueChangeEvent<Formats> event) {
                ((DateColumn) columns.get(columnListBox.getSelectedIndex())).setFormatId(dateFormat.getValue().getId());
            }
        });

        // Справочники
        refBookBox = new ValueListBox<RefBook>(new AbstractRenderer<RefBook>() {
            @Override
            public String render(RefBook refBook) {
                return refBook == null ? "" : refBook.getName();
            }
        });

        // Атрибуты справочника
        refBookAttrBox = new ValueListBox<RefBookAttribute>(new AbstractRenderer<RefBookAttribute>() {
            @Override
            public String render(RefBookAttribute refBookAttribute) {
                return refBookAttribute == null ? "" : refBookAttribute.getName();
            }
        });

        // Разыменованный атрибут справочника
        refBookAttrRefBox = new ValueListBox<RefBookAttribute>(new AbstractRenderer<RefBookAttribute>() {
            @Override
            public String render(RefBookAttribute refBookAttribute) {
                return refBookAttribute == null ? "" : refBookAttribute.getName();
            }
        });

        // Родительская графа
        refBooktAttrParentBox = new ValueListBox<Column>(new AbstractRenderer<Column>() {
            @Override
            public String render(Column column) {
                return column == null ? "" : column.getName();
            }
        });
    }

	@UiHandler("columnListBox")
	public void onSelectColumn(ChangeEvent event){
		setColumnAttributeEditor(columnListBox.getSelectedIndex());
		setUniqueParameters();
	}

	@UiHandler("upColumn")
	public void onUpColumn(ClickEvent event){
		int ind = columnListBox.getSelectedIndex();
		Column column = columns.get(ind);
		flush();

		if (column != null) {
			if (ind > 0) {
				Column exchange = columns.get(ind - 1);
				exchange.setOrder(ind + 1);
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
				exchange.setOrder(ind + 1);
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
		setColumnList();
		columnListBox.setSelectedIndex(index);
	}

	@UiHandler("addColumn")
	public void onAddColumn(ClickEvent event){
		Column newColumn = new StringColumn();
		newColumn.setName("Новый столбец");
		newColumn.setAlias("псевдоним");
		newColumn.setWidth(5);
		newColumn.setOrder(columns.size() + 1);
		getUiHandlers().addColumn(newColumn);
		setupColumns(columns.size() - 1);
	}

	@UiHandler("removeColumn")
	public void onRemoveColumn(ClickEvent event){
		int index = columnListBox.getSelectedIndex();
		getUiHandlers().removeColumn(columns.get(index));

		for (int i = index; i < columns.size(); i++) {
			columns.get(i).setOrder(columns.get(i).getOrder() - 1);
		}

		if (index > 0) {
			setupColumns(index-1);
		}
		else {
			setupColumns(0);
		}
	}

	@UiHandler("stringMaxLengthBox")
	public void onStringMaxLengthBox(KeyUpEvent event) {
        final Integer maxLength = ((StringColumn) columns.get(columnListBox.getSelectedIndex())).MAX_LENGTH;
        int length = stringMaxLengthBox.getValue();

        Timer timer = new Timer() {
            @Override
            public void run() {
                errorMsg.setText(null);
                stringMaxLengthPanel.getElement().getStyle().clearColor();
            }
        };

        if (length > maxLength) {
            errorMsg.setText("Ограничение длины " + maxLength);
            stringMaxLengthPanel.getElement().getStyle().setProperty("color", "red");
            stringMaxLengthBox.setText(maxLength.toString());
            timer.schedule(3000);
        } else {
            errorMsg.setText(null);
            stringMaxLengthPanel.getElement().getStyle().clearColor();
            stringMaxLengthBox.getElement().getStyle().clearBorderColor();
        }

        StringColumn column = ((StringColumn) columns.get(columnListBox.getSelectedIndex()));
        column.setPrevLength(column.getMaxLength());
        column.setMaxLength(stringMaxLengthBox.getValue());
	}

	@UiHandler("numericMaxLengthBox")
	public void onNumericMaxLengthBox(KeyUpEvent event) {
		((NumericColumn) columns.get(columnListBox.getSelectedIndex())).setMaxLength(numericMaxLengthBox.getValue());
	}

    @UiHandler("refBookBox")
	public void onRefBookBox(ValueChangeEvent<RefBook> event) {
        Column currentColumn = columns.get(columnListBox.getSelectedIndex());
        if (currentColumn instanceof RefBookColumn) {
            ((RefBookColumn) currentColumn).setRefBookAttributeId(event.getValue().getAttributes().get(0).getId());
        } else if (currentColumn instanceof ReferenceColumn) {
            ((ReferenceColumn) currentColumn).setRefBookAttributeId(event.getValue().getAttributes().get(0).getId());
        }
        refBookAttrBox.setValue(event.getValue().getAttributes().get(0));
        refBookAttrBox.setAcceptableValues(event.getValue().getAttributes());
	}

    @UiHandler("refBookAttrBox")
    public void onRefBookAttrBox(ValueChangeEvent<RefBookAttribute> event) {
        Column currentColumn = columns.get(columnListBox.getSelectedIndex());
        if (currentColumn instanceof RefBookColumn) {
            ((RefBookColumn) currentColumn).setRefBookAttributeId(event.getValue().getId());
        } else if (currentColumn instanceof ReferenceColumn) {
            ((ReferenceColumn) currentColumn).setRefBookAttributeId(event.getValue().getId());
        }

        if (refBookAttrBox.getValue().getAttributeType() == RefBookAttributeType.REFERENCE){
            RefBook refBookDereference = getUiHandlers().getRefBook(refBookAttrBox.getValue().getRefBookId());
            refBookAttrRefBox.setAcceptableValues(refBookDereference.getAttributes());
            refBookAttrRefBox.setVisible(true);
        }
        else{
            refBookAttrRefBox.setVisible(false);
            refBookAttrRefBox.setValue(null);
            ((ReferenceColumn) currentColumn).setRefBookAttributeId2(new Long(0));
        }
    }

    @UiHandler("refBookAttrRefBox")
    public void onRefBookAttrRefBox(ValueChangeEvent<RefBookAttribute> event) {
        Column currentColumn = columns.get(columnListBox.getSelectedIndex());
        if (currentColumn instanceof ReferenceColumn) {
            ((ReferenceColumn) currentColumn).setRefBookAttributeId2(event.getValue().getId());
        }
    }

    @UiHandler("refBookAttrFilterArea")
    public void onRefBookAttrFilterArea(KeyUpEvent event) {
        ((RefBookColumn) columns.get(columnListBox.getSelectedIndex())).setFilter(refBookAttrFilterArea.getValue());
    }

    @UiHandler("refBooktAttrParentBox")
    public void onRefBooktAttrParentBox(ValueChangeEvent<Column> event) {
        ReferenceColumn currentColumn = ((ReferenceColumn) columns.get(columnListBox.getSelectedIndex()));
        RefBookColumn parentColumn = (RefBookColumn)event.getValue();
        // Обновление списка отобр. атрибутов
        RefBook parentRefBook = getUiHandlers().getRefBook(getUiHandlers().getRefBookByAttributeId(
                parentColumn.getRefBookAttributeId()));
        List<RefBookAttribute> refBookAttributeList = parentRefBook.getAttributes();
        RefBookAttribute refBookAttribute = refBookAttributeList.get(0);
        currentColumn.setRefBookAttributeId(refBookAttribute.getId());
        currentColumn.setParentId(event.getValue().getId());
        refBookAttrBox.setValue(refBookAttribute, false);
        refBookAttrBox.setAcceptableValues(refBookAttributeList);
    }

	@Override
	public void setColumnList(List<Column> columnList, boolean isFormChanged) {
		columns = columnList;
		setAttributesPanel();

		if (columnListBox.getSelectedIndex() >= 0 && !isFormChanged) {
			setupColumns(columnListBox.getSelectedIndex());
		} else {
			setupColumns(0);
		}
	}

	@Override
	public void flush() {
		Column column = columnAttributeEditor.flush();
		if (column!=null){
			getUiHandlers().flushColumn(column);
		}
	}


	private void setupColumns(int index) {
		setColumnList();
		setAttributesPanel();
		if (columns != null && !columns.isEmpty()) {
			setColumnAttributeEditor(index);
			columnListBox.setSelectedIndex(index);
			setUniqueParameters();
		}
	}

	private void setAttributesPanel() {
        attrPanel.setVisible(columns != null && !columns.isEmpty());
	}

	private void setUniqueParameters() {
		Column column = columns.get(columnListBox.getSelectedIndex());
		if (column instanceof StringColumn) {
			typeColumnDropBox.setValue(STRING_TYPE);
		} else if (column instanceof NumericColumn) {
			typeColumnDropBox.setValue(NUMERIC_TYPE);
		} else if (column instanceof DateColumn) {
			typeColumnDropBox.setValue(DATE_TYPE);
		} else if (column instanceof RefBookColumn) {
			typeColumnDropBox.setValue(REFBOOK_TYPE);
		} else if (column instanceof ReferenceColumn) {
            typeColumnDropBox.setValue(REFERENCE_TYPE);
        } else {
			throw new IllegalStateException();
		}
		populateUniqueParameters();
		typeColumnDropBox.setAcceptableValues(columnTypeNameList);
	}

	private void populateUniqueParameters() {
		Column column = columns.get(columnListBox.getSelectedIndex());

		precisionPanel.setVisible(false);
		stringMaxLengthPanel.setVisible(false);
		numericMaxLengthPanel.setVisible(false);
		nameBox.setValue(column.getName());
		dateFormatPanel.setVisible(false);
        refBookPanel.setVisible(false);
        refBookAttrPanel.setVisible(false);
        refBookAttrFilterPanel.setVisible(false);
        refBooktAttrParentPanel.setVisible(false);
        refBookAttrRefBox.setVisible(false);

		if (STRING_TYPE.equals(typeColumnDropBox.getValue())) {
            // Строка
			int maxLength = ((StringColumn) column).getMaxLength();

			stringMaxLengthBox.setValue(maxLength);
			stringMaxLengthPanel.setVisible(true);
		} else if (typeColumnDropBox.getValue().equals(NUMERIC_TYPE)) {
            // Число
			int maxLength = ((NumericColumn) column).getMaxLength();

			numericMaxLengthPanel.setVisible(true);
			numericMaxLengthBox.setValue(maxLength);
			numericMaxLengthBox.setVisible(true);
			precisionPanel.setVisible(true);
			precisionBox.setValue(((NumericColumn) column).getPrecision());
			precisionBox.setAcceptableValues(precisionList);
		} else if (typeColumnDropBox.getValue().equals(DATE_TYPE)) {
            // Дата
			dateFormat.setAcceptableValues(dateFormatList);
			// Если формата нет, то выставляем по умолчанию DD_MM_YYYY
			dateFormat.setValue(Formats.getById(((DateColumn) column).getFormatId() == Formats.NONE.getId() ?
					Formats.DD_MM_YYYY.getId() :
					((DateColumn) column).getFormatId() ));
			dateFormatPanel.setVisible(true);

		} else if (REFBOOK_TYPE.equals(typeColumnDropBox.getValue())) {
            // Справочник
            refBookPanel.setVisible(true);
            refBookAttrPanel.setVisible(true);
            refBookAttrRefBox.setVisible(false);
            refBookAttrFilterPanel.setVisible(true);
            // Указанный атрибут
            long attributeId = ((RefBookColumn)column).getRefBookAttributeId();
            RefBookAttribute refBookAttribute = getUiHandlers().getRefBookAttribute(attributeId);
            refBookAttrBox.setValue(refBookAttribute, false);
            refBookAttrBox.setAcceptableValues(getUiHandlers().getRefBook(
                    getUiHandlers().getRefBookByAttributeId(attributeId)).getAttributes());
            // Справочник
            refBookBox.setValue(getUiHandlers().getRefBook(getUiHandlers().getRefBookByAttributeId(attributeId)), false);

            // Фильтр
            refBookAttrFilterArea.setValue(((RefBookColumn) column).getFilter(), false);
        } else if (REFERENCE_TYPE.equals(typeColumnDropBox.getValue())) {
            // Зависимая графа
            refBooktAttrParentPanel.setVisible(true);
            refBookAttrPanel.setVisible(true);
            refBookAttrRefBox.setVisible(true);
            List<Column> availableList = getRefBookColumn(null);
            if (!availableList.isEmpty()) {
                RefBookColumn parentColumn = null;
                for (Column col : availableList) {
                    if (col.getId().longValue() == ((ReferenceColumn)column).getParentId()) {
                        parentColumn = (RefBookColumn)col;
                        break;
                    }
                }
                refBooktAttrParentBox.setValue(parentColumn, false);
                refBooktAttrParentBox.setAcceptableValues(availableList);
                // Получаем Код отображаемого атрибута для столбцов-ссылок
                long attributeId = ((ReferenceColumn)column).getRefBookAttributeId();
                // Получаем этот аттребут
                RefBookAttribute refBookAttribute = getUiHandlers().getRefBookAttribute(attributeId);
                refBookAttrBox.setValue(refBookAttribute, false);
                refBookAttrBox.setAcceptableValues( getUiHandlers().getRefBook(getUiHandlers().getRefBookByAttributeId(attributeId)).getAttributes());
                // Атрибут для столбцов-ссылок второго уровня
                if (refBookAttribute.getAttributeType() == RefBookAttributeType.REFERENCE){
                    if (((ReferenceColumn)column).getRefBookAttributeId2() != null){
                        Long attributeId2 = ((ReferenceColumn)column).getRefBookAttributeId2();
                        refBookAttrRefBox.setValue(getUiHandlers().getRefBookAttribute(attributeId2),false);
                    }
                    else{
                        refBookAttrRefBox.setValue(null, false);
                    }

                    refBookAttrRefBox.setAcceptableValues(getUiHandlers().getRefBook(
                            getUiHandlers().getRefBookByAttributeId(
                                    refBookAttribute.getRefBookAttributeId())).getAttributes());
                }
                else{
                    refBookAttrRefBox.setVisible(false);
                    refBookAttrRefBox.setValue(null);
                }
            } else {
                refBooktAttrParentBox.setValue(null, false);
                refBooktAttrParentBox.setAcceptableValues(new ArrayList<Column>());
                refBookAttrBox.setValue(null, false);
                refBookAttrBox.setAcceptableValues(new ArrayList<RefBookAttribute>());
            }
        }
    }

    /**
     * Список справочных граф
     */
    private List<Column> getRefBookColumn(Column excludeColumn) {
        List<Column> refBookList = new LinkedList<Column>();
        for (Column col : columns) {
            if (excludeColumn != null && excludeColumn == col) {
                continue;
            }
            if (col instanceof RefBookColumn){
                refBookList.add(col);
            }
        }
        return refBookList;
    }

	private void setColumnList() {
		if (columns != null) {
			columnListBox.clear();
			for (Column column : columns) {
				if (column.getOrder() < 10) {
					columnListBox.addItem("0" + column.getOrder() + " " + column.getName());
				}
				else {
					columnListBox.addItem(column.getOrder() + " " + column.getName());
				}
			}
		}
	}

	private void setColumnAttributeEditor(int index) {
		flush();
		columnAttributeEditor.setValue(columns.get(index));
	}

    /**
     * Изменение типа графы
     */
	private void createNewColumnType() {
		int index = columnListBox.getSelectedIndex();
		Column column = columns.get(index);
        Column newColumn = null;
		flush();

		if (typeColumnDropBox.getValue().equals(STRING_TYPE)) {
			StringColumn stringColumn = new StringColumn();
			copyMainColumnAttributes(column, stringColumn);
            newColumn = stringColumn;
		}
		else if (NUMERIC_TYPE.equals(typeColumnDropBox.getValue())) {
			NumericColumn numericColumn = new NumericColumn();
			copyMainColumnAttributes(column, numericColumn);
            newColumn = numericColumn;
		}
		else if (DATE_TYPE.equals(typeColumnDropBox.getValue())){
			DateColumn dateColumn = new DateColumn();
			copyMainColumnAttributes(column, dateColumn);
			newColumn = dateColumn;
		} else if (REFBOOK_TYPE.equals(typeColumnDropBox.getValue())){
			RefBookColumn refBookColumn = new RefBookColumn();
            copyMainColumnAttributes(column, refBookColumn);
			newColumn = refBookColumn;
		} else if (REFERENCE_TYPE.equals(typeColumnDropBox.getValue())){
            ReferenceColumn referenceColumn = new ReferenceColumn();
            // Список справочных граф
            List<Column> availableList = getRefBookColumn(column);
            if (!availableList.isEmpty()) {
                // Берем первую из списка
                RefBookColumn parentColumn = (RefBookColumn)availableList.get(0);
                referenceColumn.setParentId(parentColumn.getId());
                RefBook parentRefBook = getUiHandlers().getRefBook(
                        getUiHandlers().getRefBookByAttributeId(parentColumn.getRefBookAttributeId()));
                referenceColumn.setRefBookAttributeId(parentRefBook.getAttributes().get(0).getId());
                copyMainColumnAttributes(column, referenceColumn);
                newColumn = referenceColumn;
            }
        }

        if (newColumn != null) {
            getUiHandlers().removeColumn(column);
            getUiHandlers().addColumn(index, newColumn);
            if (newColumn.getId() == null) {
                newColumn.setId(getUiHandlers().getNextGeneratedColumnId());
            }
        }

		populateUniqueParameters();
	}

	private void copyMainColumnAttributes(Column from, Column to) {
		to.setId(from.getId());
		to.setName(from.getName());
		to.setAlias(from.getAlias());
		to.setWidth(from.getWidth());
		to.setOrder(from.getOrder());
	}

	@Override
	public void setColumn(Column column) {
		columnAttributeEditor.setValue(column);
	}

    @Override
    public void setRefBookList(List<RefBook> refBookList) {
        if (refBookList.isEmpty())
            return;
        refBookBox.setValue(refBookList.get(0));
        refBookBox.setAcceptableValues(refBookList);
    }
}
