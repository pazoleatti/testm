package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateColumnPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.ColumnAttributeEditor;
import com.google.gwt.event.dom.client.BlurEvent;
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
import com.google.gwt.user.client.DOM;
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
    private static final String STRING_TYPE = ColumnType.STRING.getTitle();
    private static final String NUMERIC_TYPE = ColumnType.NUMBER.getTitle();
    private static final String DATE_TYPE = ColumnType.DATE.getTitle();
    private static final String REFBOOK_TYPE = ColumnType.REFBOOK.getTitle();
    private static final String REFERENCE_TYPE = ColumnType.REFERENCE.getTitle();
    private static final String AUTONUMERATION_TYPE = ColumnType.AUTO.getTitle();

	private List<Column> columns;

	private static final List<String> COLUMN_TYPE_NAME_LIST = Arrays.asList(STRING_TYPE, NUMERIC_TYPE, DATE_TYPE,
            REFBOOK_TYPE, REFERENCE_TYPE, AUTONUMERATION_TYPE);
	private static final List<Integer> PRECISION_LIST = new ArrayList<Integer>();
	private static final List<Formats> DATE_FORMAT_LIST = new ArrayList<Formats>();
    private static final List<AutoNumerationColumn> AUTO_NUMERATION_LIST = new ArrayList<AutoNumerationColumn>();

	static {
		for(int i = 0; i <= NumericColumn.MAX_PRECISION; i++) {
			PRECISION_LIST.add(i);
		}

		for (Formats f : Formats.values()) {
			if ((f != Formats.NONE) && (f != null)) {
				DATE_FORMAT_LIST.add(f);
			}
		}

        AUTO_NUMERATION_LIST.add(new AutoNumerationColumn(NumerationType.SERIAL));
        AUTO_NUMERATION_LIST.add(new AutoNumerationColumn(NumerationType.CROSS));
    }

	@UiField
	Button upColumn, downColumn, addColumn, removeColumn;

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
	Panel stringMaxLengthPanel, numericMaxLengthPanel, dateFormatPanel,
            refBookPanel, refBookAttrPanel, refBookAttrFilterPanel, refBooktAttrParentPanel, autoNumerationPanel;

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

    @UiField
    Panel refBookRefAttrPanel;

    @UiField(provided = true)
    ValueListBox<RefBookAttribute> refBookAttrRefBox;

    @UiField(provided = true)
    ValueListBox<Column> refBooktAttrParentBox;

    @UiField(provided = true)
    ValueListBox<AutoNumerationColumn> autoNumerationBox;

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
                if (event.getValue()+numericMaxLengthBox.getValue()>NumericColumn.MAX_LENGTH){
                    precisionBox.setValue(NumericColumn.MAX_LENGTH - numericMaxLengthBox.getValue(), false);
                }
				setNumValueRestrictions(numericMaxLengthBox.getValue(), precisionBox.getValue());
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

        // Автонумеруемая графа
        autoNumerationBox = new ValueListBox<AutoNumerationColumn>(new AbstractRenderer<AutoNumerationColumn>() {
            @Override
            public String render(AutoNumerationColumn object) {
                return object.getNumerationType().getTitle();
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
	public void onStringMaxLengthBox(BlurEvent event) {
        final Integer maxLength = StringColumn.MAX_LENGTH;
        int length = stringMaxLengthBox.getValue() != null?stringMaxLengthBox.getValue():0;
        if (length == 0){
            event.getRelativeElement().getStyle().setBackgroundColor("#ffccd2");
            return;
        } else{
            event.getRelativeElement().getStyle().setBackgroundColor("");
        }

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
	public void onNumericMaxLengthBox(ValueChangeEvent<Integer> event) {
        if (event.getValue().compareTo(NumericColumn.MAX_LENGTH) > 0){
            numericMaxLengthBox.setValue(NumericColumn.MAX_LENGTH);
        }
		if (event.getValue() + precisionBox.getValue() > NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION) {
			numericMaxLengthBox.setValue(NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION + precisionBox.getValue());
		}
		setNumValueRestrictions(numericMaxLengthBox.getValue(), precisionBox.getValue());
	}

	@UiHandler("precisionBox")
	public void onPrecisionBox(ValueChangeEvent<Integer> event) {
		if (event.getValue() + NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION < numericMaxLengthBox.getValue()) {
			numericMaxLengthBox.setValue(NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION + precisionBox.getValue());
		}
		setNumValueRestrictions(numericMaxLengthBox.getValue(), precisionBox.getValue());
	}

    @UiHandler("refBookBox")
	public void onRefBookBox(ValueChangeEvent<RefBook> event) {
        Column currentColumn = columns.get(columnListBox.getSelectedIndex());
        if (ColumnType.REFBOOK.equals(currentColumn.getColumnType())) {
            ((RefBookColumn) currentColumn).setRefBookAttributeId(event.getValue().getAttributes().get(0).getId());
        } else if (ColumnType.REFERENCE.equals(currentColumn.getColumnType())) {
            ((ReferenceColumn) currentColumn).setRefBookAttributeId(event.getValue().getAttributes().get(0).getId());
        }
        refBookAttrBox.setValue(event.getValue().getAttributes().get(0));
        refBookAttrBox.setAcceptableValues(event.getValue().getAttributes());
        updateReferenceColumn(event.getValue(), currentColumn);
	}

    @UiHandler("refBookAttrBox")
    public void onRefBookAttrBox(ValueChangeEvent<RefBookAttribute> event) {
        if (event.getValue()==null){
            return;
        }
        Column currentColumn = columns.get(columnListBox.getSelectedIndex());
        if (ColumnType.REFBOOK.equals(currentColumn.getColumnType())) {
            ((RefBookColumn) currentColumn).setRefBookAttributeId(event.getValue().getId());
        } else if (ColumnType.REFERENCE.equals(currentColumn.getColumnType())) {
            ((ReferenceColumn) currentColumn).setRefBookAttributeId(event.getValue().getId());
        }

        if (refBookAttrBox.getValue().getAttributeType() == RefBookAttributeType.REFERENCE){
            RefBook refBookDereference = getUiHandlers().getRefBook(refBookAttrBox.getValue().getRefBookId());
            refBookAttrRefBox.setValue(null);
            refBookAttrRefBox.setAcceptableValues(refBookDereference.getAttributes());
            refBookRefAttrPanel.setVisible(true);
            refBookAttrRefBox.setVisible(true);
        }
        else{
            refBookRefAttrPanel.setVisible(false);
            refBookAttrRefBox.setVisible(false);
            refBookAttrRefBox.setValue(null);
            if (currentColumn instanceof RefBookColumn)
                ((RefBookColumn) currentColumn).setRefBookAttributeId2(null);
        }
    }

    @UiHandler("refBookAttrRefBox")
    public void onRefBookAttrRefBox(ValueChangeEvent<RefBookAttribute> event) {
        Column currentColumn = columns.get(columnListBox.getSelectedIndex());
        if (ColumnType.REFERENCE.equals(currentColumn.getColumnType())) {
            ((ReferenceColumn) currentColumn).setRefBookAttributeId2(event.getValue().getId());
        } else if (ColumnType.REFBOOK.equals(currentColumn.getColumnType())) {
            ((RefBookColumn) currentColumn).setRefBookAttributeId2(event.getValue().getId());
        }
    }

    @UiHandler("refBookAttrFilterArea")
    public void onRefBookAttrFilterArea(KeyUpEvent event) {
        ((FilterColumn) columns.get(columnListBox.getSelectedIndex())).setFilter(refBookAttrFilterArea.getValue());
    }

    @UiHandler("refBooktAttrParentBox")
    public void onRefBooktAttrParentBox(ValueChangeEvent<Column> event) {
        ReferenceColumn currentColumn = ((ReferenceColumn) columns.get(columnListBox.getSelectedIndex()));
        RefBookColumn parentColumn = (RefBookColumn)event.getValue();
        // Обновление списка отобр. атрибутов
        RefBook parentRefBook = getUiHandlers().getRefBook(getUiHandlers().getRefBookByAttributeId(
                parentColumn.getRefBookAttributeId(), false));
        List<RefBookAttribute> refBookAttributeList = parentRefBook.getAttributes();
        RefBookAttribute refBookAttribute = refBookAttributeList.get(0);
        currentColumn.setRefBookAttributeId(refBookAttribute.getId());
        currentColumn.setParentId(event.getValue().getId());
        refBookAttrBox.setValue(refBookAttribute, false);
        refBookAttrBox.setAcceptableValues(refBookAttributeList);
    }

    @UiHandler("autoNumerationBox")
    public void onAutoNumerationBox(ValueChangeEvent<AutoNumerationColumn> event) {
        ((AutoNumerationColumn) columns.get(columnListBox.getSelectedIndex())).setNumerationType(event.getValue().getNumerationType());
    }

	@Override
	public final void setColumnList(List<Column> columnList, boolean isFormChanged) {
		columns = columnList;
		setAttributesPanel();

		if (columnListBox.getSelectedIndex() >= 0 && !isFormChanged) {
			setupColumns(columnListBox.getSelectedIndex());
		} else {
			setupColumns(0);
		}
	}

	@Override
	public final void flush() {
		Column column = columnAttributeEditor.flush();
		if (column!=null){
			getUiHandlers().flushColumn(column);
		}
	}

    @Override
    public void setEnableModify(boolean isEnable) {
        addColumn.setEnabled(isEnable);
        removeColumn.setEnabled(isEnable);
        upColumn.setEnabled(isEnable);
        downColumn.setEnabled(isEnable);
        DOM.setElementPropertyBoolean(typeColumnDropBox.getElement(), "disabled", !isEnable);
        columnAttributeEditor.setEnabled(isEnable);
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
		switch (column.getColumnType()) {
			case STRING:
				typeColumnDropBox.setValue(STRING_TYPE);
				break;
			case NUMBER:
				typeColumnDropBox.setValue(NUMERIC_TYPE);
				break;
			case DATE:
				typeColumnDropBox.setValue(DATE_TYPE);
				break;
			case REFBOOK:
				typeColumnDropBox.setValue(REFBOOK_TYPE);
				break;
			case REFERENCE:
				typeColumnDropBox.setValue(REFERENCE_TYPE);
				break;
			case AUTO:
				typeColumnDropBox.setValue(AUTONUMERATION_TYPE);
				break;
			default:
				throw new IllegalStateException();
		}
		populateUniqueParameters();
		typeColumnDropBox.setAcceptableValues(COLUMN_TYPE_NAME_LIST);
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
        refBookRefAttrPanel.setVisible(false);
        refBookAttrRefBox.setVisible(false);
        autoNumerationPanel.setVisible(false);

		if (STRING_TYPE.equals(typeColumnDropBox.getValue())) {
            // Строка
			int maxLength = ((StringColumn) column).getMaxLength();

			stringMaxLengthBox.setValue(maxLength);
			stringMaxLengthPanel.setVisible(true);
			refBookAttrFilterPanel.setVisible(true);
			refBookAttrFilterArea.setValue(((StringColumn) column).getFilter(), false);
		} else if (typeColumnDropBox.getValue().equals(NUMERIC_TYPE)) {
            // Число
			int maxLength = ((NumericColumn) column).getMaxLength();

			numericMaxLengthPanel.setVisible(true);
			numericMaxLengthBox.setValue(maxLength);
			numericMaxLengthBox.setVisible(true);
			precisionPanel.setVisible(true);
			precisionBox.setValue(((NumericColumn) column).getPrecision());
			precisionBox.setAcceptableValues(PRECISION_LIST);
		} else if (typeColumnDropBox.getValue().equals(DATE_TYPE)) {
            // Дата
			dateFormat.setAcceptableValues(DATE_FORMAT_LIST);
			// Если формата нет, то выставляем по умолчанию DD_MM_YYYY
			dateFormat.setValue(Formats.getById(((DateColumn) column).getFormatId() == Formats.NONE.getId() ?
					Formats.DD_MM_YYYY.getId() :
					((DateColumn) column).getFormatId() ));
			dateFormatPanel.setVisible(true);

		} else if (REFBOOK_TYPE.equals(typeColumnDropBox.getValue())) {
            // Справочник
            refBookPanel.setVisible(true);
            refBookAttrPanel.setVisible(true);
            refBookRefAttrPanel.setVisible(false);
            refBookAttrRefBox.setVisible(false);
            refBookAttrFilterPanel.setVisible(true);
            // Указанный атрибут
            Long attributeId = ((RefBookColumn)column).getRefBookAttributeId();
            RefBookAttribute refBookAttribute = getUiHandlers().getRefBookAttribute(attributeId);
            RefBook refBook = getUiHandlers().getRefBook(getUiHandlers().getRefBookByAttributeId(attributeId, true));
            refBookAttrBox.setValue(
                    refBookAttribute == null ?
                            refBook.getAttributes() != null && !refBook.getAttributes().isEmpty() ?
                                    refBook.getAttributes().get(0) : null
                            : refBookAttribute,
                    true);
            refBookAttrBox.setAcceptableValues(refBook.getAttributes());
            if (refBookAttribute != null && refBookAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                refBookRefAttrPanel.setVisible(true);
                refBookAttrRefBox.setVisible(true);

                if (((RefBookColumn)column).getRefBookAttributeId2() != null){
                    Long attributeId2 = ((RefBookColumn)column).getRefBookAttributeId2();
                    refBookAttrRefBox.setValue(getUiHandlers().getRefBookAttribute(attributeId2),false);
                }
                else{
                    refBookAttrRefBox.setValue(null, false);
                }

                refBookAttrRefBox.setAcceptableValues(getUiHandlers().getRefBook(
                        getUiHandlers().getRefBookByAttributeId(
                                refBookAttribute.getRefBookAttributeId(), false)).getAttributes());
            }
            // Справочник
            refBookBox.setValue(refBook, false);

            // Фильтр
            refBookAttrFilterArea.setValue(((RefBookColumn) column).getFilter(), false);
        } else if (REFERENCE_TYPE.equals(typeColumnDropBox.getValue())) {
            // Зависимая графа
            refBooktAttrParentPanel.setVisible(true);
            refBookAttrPanel.setVisible(true);
            refBookRefAttrPanel.setVisible(true);
            refBookAttrRefBox.setVisible(true);
            List<Column> availableList = getRefBookColumn(column);
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
                refBookAttrBox.setAcceptableValues( getUiHandlers().getRefBook(getUiHandlers().getRefBookByAttributeId(attributeId, false)).getAttributes());
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
                                    refBookAttribute.getRefBookAttributeId(), false)).getAttributes());
                }
                else{
                    refBookRefAttrPanel.setVisible(false);
                    refBookAttrRefBox.setVisible(false);
                    refBookAttrRefBox.setValue(null);
                }
            } else {
                refBooktAttrParentBox.setValue(null, false);
                refBooktAttrParentBox.setAcceptableValues(new ArrayList<Column>());
                refBookAttrBox.setValue(null, false);
                refBookAttrBox.setAcceptableValues(new ArrayList<RefBookAttribute>());
            }
        } else if (AUTONUMERATION_TYPE.equals(typeColumnDropBox.getValue())) {
            autoNumerationPanel.setVisible(true);
            autoNumerationBox.setVisible(true);
            autoNumerationBox.setValue(AUTO_NUMERATION_LIST.get(((AutoNumerationColumn) column).getNumerationType().getId()), false);
            autoNumerationBox.setAcceptableValues(AUTO_NUMERATION_LIST);
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
            RefBook refBook = getUiHandlers().getRefBook(getUiHandlers().getRefBookByAttributeId(null, true));
            refBookColumn.setRefBookAttributeId(refBook.getAttributes().get(0).getId());
			newColumn = refBookColumn;
		} else if (REFERENCE_TYPE.equals(typeColumnDropBox.getValue())){
            ReferenceColumn referenceColumn = new ReferenceColumn();
            // Список справочных граф
            List<Column> availableList = getRefBookColumn(column);
            if (!availableList.isEmpty()) {
                // Берем первую из списка
                RefBookColumn parentColumn = (RefBookColumn)availableList.get(0);
                referenceColumn.setParentId(parentColumn.getId());
                referenceColumn.setParentAlias(parentColumn.getAlias());
                RefBook parentRefBook = getUiHandlers().getRefBook(
                        getUiHandlers().getRefBookByAttributeId(parentColumn.getRefBookAttributeId(), false));
                if (parentRefBook!=null){
                    referenceColumn.setRefBookAttributeId(parentRefBook.getAttributes().get(0).getId());
                }
                copyMainColumnAttributes(column, referenceColumn);
                newColumn = referenceColumn;
            }
        } else if (AUTONUMERATION_TYPE.equals(typeColumnDropBox.getValue())) {
            AutoNumerationColumn autoNumerationColumn = new AutoNumerationColumn();
            copyMainColumnAttributes(column, autoNumerationColumn);
            newColumn = autoNumerationColumn;
        }

        if (newColumn != null) {
            getUiHandlers().changeColumnType(index, column, newColumn);
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
	public final void setColumn(Column column) {
		columnAttributeEditor.setValue(column);
	}

    @Override
    public final void setRefBookList(List<RefBook> refBookList) {
        if (refBookList == null || refBookList.isEmpty())
            return;
        refBookBox.setValue(refBookList.get(0));
        refBookBox.setAcceptableValues(refBookList);
    }

    private void updateReferenceColumn(RefBook refBook, Column currColumn){
        for (Column column : columns){
            if (column instanceof ReferenceColumn && ((ReferenceColumn) column).getParentAlias().equals(currColumn.getAlias())){
                ((ReferenceColumn)column).setRefBookAttributeId(refBook.getAttributes().get(0).getId());
            }
        }
    }
    
    private void setNumValueRestrictions(int unscaledValue, int scale){
        ((NumericColumn) columns.get(columnListBox.getSelectedIndex())).setMaxLength(unscaledValue);
        ((NumericColumn)columns.get(columnListBox.getSelectedIndex())).setPrecision(scale);
    }
}
