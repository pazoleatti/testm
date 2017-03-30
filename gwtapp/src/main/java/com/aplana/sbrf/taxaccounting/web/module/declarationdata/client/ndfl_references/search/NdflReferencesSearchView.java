package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.search;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.Date;
import java.util.List;

import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;

public class NdflReferencesSearchView extends PopupViewWithUiHandlers<NdflReferencesSearchUIHandlers> implements NdflReferencesSearchPresenter.SearchView {
    interface Binder extends UiBinder<PopupPanel, NdflReferencesSearchView> {
    }

    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy");

    private final PopupPanel widget;

    @UiField
    ModalWindow modalWindow;
    @UiField
    GenericDataGrid<DataRow<Cell>> table;
    @UiField
    FlexiblePager pager;
    @UiField
    TextBox refNumberTextBox, firstNameTextBox, lastNameTextBox, middleNameTextBox;
    @UiField
    DateMaskBoxPicker birthDateFromTextBox, birthDateBeforeTextBox;
    @UiField
    Button findButton, clearButton, chooseButton, closeButton;

    private MultiSelectionModel<DataRow<Cell>> selectionModel = new MultiSelectionModel<DataRow<Cell>>();
    private CheckBoxHeader checkBoxHeader = new CheckBoxHeader();
    private DefaultSelectionEventManager<DataRow<Cell>> multiSelectManager = createCustomManager(
            new DefaultSelectionEventManager.CheckboxEventTranslator<DataRow<Cell>>()
    );

    private NumericColumn ndflReferenceIdColumn = new NumericColumn();
    private StringColumn refNumberColumn = new StringColumn();
    private StringColumn lastNameColumn = new StringColumn();
    private StringColumn firstNameColumn = new StringColumn();
    private StringColumn middleNameColumn = new StringColumn();
    private StringColumn birthDateColumn = new StringColumn();
    private StringColumn taxServiceTextColumn = new StringColumn();

    @Inject
    public NdflReferencesSearchView(NdflReferencesSearchView.Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        selectionModel.addSelectionChangeHandler(
                new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        onSelection();
                    }
                });
        table.setSelectionModel(selectionModel, multiSelectManager);
        checkBoxHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                for (DataRow<Cell> row : table.getVisibleItems()) {
                    selectionModel.setSelected(row, event.getValue());
                }
            }
        });
        initColumns();
        pager.setDisplay(table);
    }

    private void onSelection() {
        int selectedItemCount = selectionModel.getSelectedSet().size();
        int visibleItemCount = table.getVisibleItemCount();

        if (selectedItemCount < visibleItemCount || selectedItemCount == 0) {
            checkBoxHeader.setValue(false);
        } else if (selectedItemCount == visibleItemCount) {
            checkBoxHeader.setValue(true);
        }

    }

    private void initColumns() {
        table.removeAllColumns();

        com.google.gwt.user.cellview.client.Column<DataRow<Cell>, Boolean> checkColumn = new com.google.gwt.user.cellview.client.Column<DataRow<Cell>, Boolean>(
                new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(DataRow<Cell> object) {
                return selectionModel.isSelected(object);
            }
        };

        ndflReferenceIdColumn.setAlias("ndflReferenceIdColumn");
        ndflReferenceIdColumn.setName("ndflReferenceIdColumn");

        refNumberColumn.setAlias("refNumber");
        refNumberColumn.setName("Номер справки");
        DataRowColumn<String> refNumberColumnUI = new DataRowColumn<String>(new TextCell(), refNumberColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                return object.getCell(refNumberColumn.getAlias()).getStringValue();
            }
        };

        lastNameColumn.setAlias("lastName");
        lastNameColumn.setName("Фамилия");
        DataRowColumn<String> lastNameColumnUI = new DataRowColumn<String>(new TextCell(), lastNameColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                return object.getCell(lastNameColumn.getAlias()).getStringValue();
            }
        };

        firstNameColumn.setAlias("firstName");
        firstNameColumn.setName("Имя");
        DataRowColumn<String> firstNameColumnUI = new DataRowColumn<String>(new TextCell(), firstNameColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                return object.getCell(firstNameColumn.getAlias()).getStringValue();
            }
        };

        middleNameColumn.setAlias("middleName");
        middleNameColumn.setName("Отчество");
        DataRowColumn<String> middleNameColumnUI = new DataRowColumn<String>(new TextCell(), middleNameColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                return object.getCell(middleNameColumn.getAlias()).getStringValue();
            }
        };

        birthDateColumn.setAlias("birthDate");
        birthDateColumn.setName("Дата рождения");
        DataRowColumn<String> birthDateColumnUI = new DataRowColumn<String>(new TextCell(), birthDateColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                return format.format(object.getCell(birthDateColumn.getAlias()).getDateValue());
            }
        };

        taxServiceTextColumn.setAlias("taxServiceText");
        taxServiceTextColumn.setName("Текст ошибки от ФНС");
        DataRowColumn<String> taxServiceTextColumnUI = new DataRowColumn<String>(new TextCell(), taxServiceTextColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                return object.getCell(taxServiceTextColumn.getAlias()).getStringValue();
            }
        };

        table.addColumn(checkColumn, checkBoxHeader);
        table.setColumnWidth(checkColumn, 2, Style.Unit.EM);
        table.addColumn(refNumberColumnUI, refNumberColumn.getName());
        table.addColumn(lastNameColumnUI, lastNameColumn.getName());
        table.addColumn(firstNameColumnUI, firstNameColumn.getName());
        table.addColumn(middleNameColumnUI, middleNameColumn.getName());
        table.addColumn(birthDateColumnUI, birthDateColumn.getName());
        table.addColumn(taxServiceTextColumnUI, taxServiceTextColumn.getName());
    }

    @UiHandler("findButton")
    public void onFindClicked(ClickEvent event) {
        getUiHandlers().onFindClicked();
    }

    @UiHandler("clearButton")
    public void onClearClicked(ClickEvent event) {
        getUiHandlers().onClearClicked();
    }

    @UiHandler("chooseButton")
    public void onChooseClicked(ClickEvent event) {
        getUiHandlers().onChooseClicked(selectionModel.getSelectedSet());
    }

    @UiHandler("closeButton")
    public void onCloseClicked(ClickEvent event) {
        getUiHandlers().onCloseClicked();
    }

    @Override
    public GenericDataGrid<DataRow<Cell>> getSearchTable() {
        return table;
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setTableData(List<DataRow<Cell>> result) {
        table.redraw();
    }

    @Override
    public String getRefNumberValue() {
        return refNumberTextBox.getValue();
    }

    @Override
    public String getLastNameValue() {
        return lastNameTextBox.getValue();
    }

    @Override
    public String getFirstNameValue() {
        return firstNameTextBox.getValue();
    }

    @Override
    public String getMiddleNameValue() {
        return middleNameTextBox.getValue();
    }

    @Override
    public Date getBirthDateFromValue() {
        return birthDateFromTextBox.getValue();
    }

    @Override
    public Date getBirthDateBeforeValue() {
        return birthDateBeforeTextBox.getValue();
    }

    @Override
    public void updateTable() {
        selectionModel.clear();
        table.setVisibleRangeAndClearData(new Range(pager.getPageStart(), pager.getPageSize()), true);
    }

    public CheckBoxHeader getCheckBoxHeader() {
        return checkBoxHeader;
    }

    @Override
    public NumericColumn getNdflReferenceIdColumn() {
        return ndflReferenceIdColumn;
    }

    @Override
    public StringColumn getRefNumberColumn() {
        return refNumberColumn;
    }
    @Override
    public StringColumn getLastNameColumn() {
        return lastNameColumn;
    }
    @Override
    public StringColumn getFirstNameColumn() {
        return firstNameColumn;
    }
    @Override
    public StringColumn getMiddleNameColumn() {
        return middleNameColumn;
    }
    @Override
    public StringColumn getBirthDateColumn() {
        return birthDateColumn;
    }
    @Override
    public StringColumn getTaxServiceTextColumn() {
        return taxServiceTextColumn;
    }

    @Override
    public TextBox getRefNumberTextBox() {
        return refNumberTextBox;
    }

    @Override
    public TextBox getLastNameTextBox() {
        return lastNameTextBox;
    }

    @Override
    public TextBox getFirstNameTextBox() {
        return firstNameTextBox;
    }

    @Override
    public TextBox getMiddleNameTextBox() {
        return middleNameTextBox;
    }

    @Override
    public DateMaskBoxPicker birthDateFromPicker() {
        return birthDateFromTextBox;
    }

    @Override
    public DateMaskBoxPicker birthDateBeforePicker() {
        return birthDateBeforeTextBox;
    }

    @Override
    public MultiSelectionModel<DataRow<Cell>> getSelectionModel() {
        return selectionModel;
    }
}