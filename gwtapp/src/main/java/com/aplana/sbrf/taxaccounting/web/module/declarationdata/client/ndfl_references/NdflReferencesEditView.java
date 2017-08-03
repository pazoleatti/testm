package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
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
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;

public class NdflReferencesEditView extends PopupViewWithUiHandlers<NdflReferencesEditUiHandlers> implements NdflReferencesEditPresenter.MyView {


    public interface Binder extends UiBinder<PopupPanel, NdflReferencesEditView> {
    }

    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy");
    private static final int NOTE_MAX_LENGTH = 255;

    private final PopupPanel widget;

    @UiField
    ModalWindow modalWindow;
    @UiField
    GenericDataGrid<DataRow<Cell>> table;
    @UiField
    com.google.gwt.user.client.ui.TextArea note;
    @UiField
    HorizontalPanel buttonPanel;
    @UiField
    LinkButton addEntry, removeEntry;
    @UiField
    Button saveButton, cancelButton;

    private ListDataProvider<DataRow<Cell>> dataProvider = new ListDataProvider<DataRow<Cell>>();
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
    private DateColumn birthDateColumn = new DateColumn();
    private StringColumn taxServiceTextColumn = new StringColumn();

    @Inject
    public NdflReferencesEditView(NdflReferencesEditView.Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        note.getElement().setAttribute("maxLength", "1000");
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
        table.setRowCount(0);
        dataProvider.addDataDisplay(table);
    }

    @UiHandler("addEntry")
    public void onAddClicked(ClickEvent event) {
        getUiHandlers().onAddClicked();
    }

    @UiHandler("removeEntry")
    public void onRemoveFileClicked(ClickEvent event) {
        Set<DataRow<Cell>> selectedItems = selectionModel.getSelectedSet();
        getUiHandlers().onRemoveClicked(selectedItems);
    }

    @UiHandler("saveButton")
    public void onSaveClicked(ClickEvent event) {
        getUiHandlers().onSaveClicked(note.getText(), new ArrayList<DataRow<Cell>>(selectionModel.getSelectedSet()), false);
    }

    @UiHandler("cancelButton")
    public void onCloseClicked(ClickEvent event) {
        getUiHandlers().onCancelClicked();
    }

    private void onSelection() {
        int selectedItemCount = selectionModel.getSelectedSet().size();
        int visibleItemCount = table.getVisibleItemCount();

        if (selectedItemCount < visibleItemCount || selectedItemCount == 0) {
            checkBoxHeader.setValue(false);
        } else if (selectedItemCount == visibleItemCount) {
            checkBoxHeader.setValue(true);
        }

        if (selectedItemCount > 0) {
            removeEntry.setEnabled(true);
        } else {
            removeEntry.setEnabled(false);
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

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setTableData(List<DataRow<Cell>> result) {
        if (result == null) {
            table.setRowCount(0);
            table.setRowData(new ArrayList<DataRow<Cell>>());
        } else {
            table.setRowData(table.getVisibleItemCount(), result);
        }
    }

    @Override
    public void setReadOnlyMode(boolean readOnlyMode) {
        if (readOnlyMode) {
            buttonPanel.setVisible(false);
            saveButton.setVisible(false);
            note.getElement().setAttribute("readonly", "readonly");
        } else {
            buttonPanel.setVisible(true);
            saveButton.setVisible(true);
            note.getElement().removeAttribute("readonly");
        }
        initColumns();
        table.redraw();
    }

    @Override
    public void setNote(String note) {
        this.note.setValue(note);
    }

    @Override
    public GenericDataGrid<DataRow<Cell>> getTable() {
        return table;
    }

    @Override
    public void updateTable(int size) {
        selectionModel.clear();
        table.setVisibleRangeAndClearData(new Range(0, size), true);
    }

    public CheckBoxHeader getCheckBoxHeader() {
        return checkBoxHeader;
    }

    @Override
    public NumericColumn getNdflReferenceIdColumn() {
        return ndflReferenceIdColumn;
    }

    public StringColumn getRefNumberColumn() {
        return refNumberColumn;
    }

    public StringColumn getLastNameColumn() {
        return lastNameColumn;
    }

    public StringColumn getFirstNameColumn() {
        return firstNameColumn;
    }

    public StringColumn getMiddleNameColumn() {
        return middleNameColumn;
    }

    public DateColumn getBirthDateColumn() {
        return birthDateColumn;
    }

    public StringColumn getTaxServiceTextColumn() {
        return taxServiceTextColumn;
    }

}