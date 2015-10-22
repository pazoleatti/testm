package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client;

import com.aplana.gwt.client.DoubleBox;
import com.aplana.gwt.client.TextBox;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.TableCell;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.math.BigDecimal;
import java.util.*;

/**
 * View для формы настроек подразделений
 *
 * @author Stetsenko Eugene
 */
public class DepartmentConfigPropertyView extends ViewWithUiHandlers<DepartmentConfigPropertyUiHandlers>
        implements DepartmentConfigPropertyPresenter.MyView {

    @Override
    public boolean isAscSorting() {
        return false;
    }

    @Override
    public void setSortByColumn(String dataStoreName) {
    }

    private TableHeader[] tableHeaderProperty = new TableHeader[]{
            new TableHeader("TAX_ORGAN_CODE"),
            new TableHeader("KPP"),
            new TableHeader("TAX_PLACE_TYPE_CODE"),
            new TableHeader("NAME"),
            new TableHeader("OKVED_CODE"),
            new TableHeader("PHONE"),
            new TableHeader("REORG_FORM_CODE"),
            new TableHeader("REORG_INN"),
            new TableHeader("REORG_KPP"),
            new TableHeader("SIGNATORY_ID"),
            new TableHeader("SIGNATORY_SURNAME"),
            new TableHeader("SIGNATORY_FIRSTNAME"),
            new TableHeader("SIGNATORY_LASTNAME"),
            new TableHeader("APPROVE_DOC_NAME"),
            new TableHeader("APPROVE_ORG_NAME")
    };

    private TableHeader[] tableHeaderTransport = new TableHeader[]{
            new TableHeader("TAX_ORGAN_CODE"),
            new TableHeader("KPP"),
            new TableHeader("TAX_PLACE_TYPE_CODE"),
            new TableHeader("NAME"),
            new TableHeader("OKVED_CODE"),
            new TableHeader("PHONE"),
            new TableHeader("PREPAYMENT"),
            new TableHeader("REORG_FORM_CODE"),
            new TableHeader("REORG_INN"),
            new TableHeader("REORG_KPP"),
            new TableHeader("SIGNATORY_ID"),
            new TableHeader("SIGNATORY_SURNAME"),
            new TableHeader("SIGNATORY_FIRSTNAME"),
            new TableHeader("SIGNATORY_LASTNAME"),
            new TableHeader("APPROVE_DOC_NAME"),
            new TableHeader("APPROVE_ORG_NAME")
    };

    private TableHeader[] tableHeaderIncome = new TableHeader[]{
            new TableHeader("TAX_ORGAN_CODE"),
            new TableHeader("KPP"),
            new TableHeader("TAX_ORGAN_CODE_PROM"),
            new TableHeader("TAX_PLACE_TYPE_CODE"),
            new TableHeader("NAME"),
            new TableHeader("ADDITIONAL_NAME"),
            new TableHeader("OKVED_CODE"),
            new TableHeader("DICT_REGION_ID"),
            new TableHeader("OKTMO"),
            new TableHeader("PHONE"),
            new TableHeader("OBLIGATION"),
            new TableHeader("TYPE"),
            new TableHeader("REORG_FORM_CODE"),
            new TableHeader("REORG_INN"),
            new TableHeader("REORG_KPP"),
            new TableHeader("SIGNATORY_ID"),
            new TableHeader("SIGNATORY_SURNAME"),
            new TableHeader("SIGNATORY_FIRSTNAME"),
            new TableHeader("SIGNATORY_LASTNAME"),
            new TableHeader("APPROVE_DOC_NAME"),
            new TableHeader("APPROVE_ORG_NAME")
    };

    public final class TableHeader {
        String name;

        private TableHeader(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }

    public TaxType taxType;

    interface Binder extends UiBinder<Widget, DepartmentConfigPropertyView> {
    }

    @UiField
    DataGrid<DataRow<Cell>> table;
    @UiField
    TextBox inn;
    @UiField
    TextBox formatVersion;
    @UiField
    TextBox version;
    @UiField
    DoubleBox taxRate;
    @UiField
    Button saveButton;
    @UiField
    Button deleteButton;
    @UiField
    Button cancelButton;
    @UiField
    Button editButton;
    @UiField
    LinkAnchor addLink;
    @UiField
    LinkAnchor delLink;
    @UiField
    Label taxTypeLbl;
    @UiField
    Panel versionBlock;
    @UiField
    Panel taxRateBlock;

    @UiField
    Label editModeLabel;
    @UiField
    Button findButton;

    // Выбранное подразделение
    private Integer currentDepartmentId;

    // Выбранный период
    private Integer currentReportPeriodId;

    private Boolean isUnp;

    private List<com.aplana.sbrf.taxaccounting.model.Column> columns = new ArrayList<com.aplana.sbrf.taxaccounting.model.Column>();

    private RefBookColumn paramColumn = new RefBookColumn();

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    PeriodPickerPopupWidget periodPickerPopup;

    MultiSelectionModel<DataRow<Cell>> selectionModel = new MultiSelectionModel<DataRow<Cell>>();

    List<DataRow<Cell>> checkedRows = new LinkedList<DataRow<Cell>>();

    ListDataProvider<DataRow<Cell>> model;
    Column<DataRow<Cell>, Boolean> checkColumn;

    private HandlerRegistration resizeHandler;
    private Timer resizeTimer;

    boolean isEditMode = false;
    boolean isFieldsModified = false;

    @Inject
    @UiConstructor
    public DepartmentConfigPropertyView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        ValueChangeHandler<String> valueChangeHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                isFieldsModified = true;
            }
        };

        ValueChangeHandler<Double> valueChangeHandlerDouble = new ValueChangeHandler<Double>() {
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                isFieldsModified = true;
            }
        };

        periodPickerPopup.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> event) {
                Integer selPeriodId = null;
                if (event.getValue() != null && event.getValue().size() == 1) {
                    selPeriodId = event.getValue().get(0);
                }
                // Проверка совпадения выбранного периода с текущим
                if (DepartmentConfigPropertyView.this.currentReportPeriodId != null
                        && DepartmentConfigPropertyView.this.currentReportPeriodId.equals(selPeriodId)) {
                    return;
                }
                DepartmentConfigPropertyView.this.currentReportPeriodId = selPeriodId;
                editButton.setEnabled(false);
                if (currentReportPeriodId != null && currentDepartmentId != null) {
                    getUiHandlers().getRefBookPeriod(currentReportPeriodId, currentDepartmentId);
                }
            }
        });

        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> event) {
                Integer selDepartmentId = null;
                if (event != null && !event.getValue().isEmpty()) {
                    selDepartmentId = event.getValue().iterator().next();
                }
                // Проверка совпадения выбранного подразделения с текущим
                if (DepartmentConfigPropertyView.this.currentDepartmentId != null
                        && DepartmentConfigPropertyView.this.currentDepartmentId.equals(selDepartmentId)) {
                    return;
                }
                DepartmentConfigPropertyView.this.currentDepartmentId = selDepartmentId;
                editButton.setEnabled(false);
                if (currentReportPeriodId != null && currentDepartmentId != null) {
                    getUiHandlers().getRefBookPeriod(currentReportPeriodId, currentDepartmentId);
                }
            }
        });


        inn.addValueChangeHandler(valueChangeHandler);
        formatVersion.addValueChangeHandler(valueChangeHandler);
        version.addValueChangeHandler(valueChangeHandler);
        taxRate.addValueChangeHandler(valueChangeHandlerDouble);
        resizeTimer = new Timer() {
            @Override
            public void run() {
                ellipsizeDepartmentPickerLabel(departmentPicker.isEnabled());
            }
        };
    }

    private void initTable(TaxType taxType) {

        if (taxType == TaxType.PROPERTY) {
            ConstPropertyHeaderBuilder hb = new ConstPropertyHeaderBuilder(table);
            hb.setNeedCheckedRow(false);
            table.setHeaderBuilder(hb);
        } else if (taxType == TaxType.TRANSPORT) {
            ConstTransportHeaderBuilder hb = new ConstTransportHeaderBuilder(table);
            hb.setNeedCheckedRow(false);
            table.setHeaderBuilder(hb);
        } else if (taxType == TaxType.INCOME) {
            ConstIncomeHeaderBuilder hb = new ConstIncomeHeaderBuilder(table);
            hb.setNeedCheckedRow(false);
            table.setHeaderBuilder(hb);
        }
        table.setSelectionModel(selectionModel);

        if (table.getHeaderBuilder() instanceof TableWithCheckedColumn) {
            ((TableWithCheckedColumn) table.getHeaderBuilder()).getCheckBoxHeader().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if (event.getValue()) {
                        for (DataRow<Cell> object : table.getVisibleItems()) {
                            selectionModel.setSelected(object, true);
                            checkedRows.add(object);
                        }
                    } else {
                        selectionModel.clear();
                        checkedRows.clear();
                    }
                }
            });
        }

        model = new ListDataProvider<DataRow<Cell>>();
        model.addDataDisplay(table);
    }

    private void removeAllColumns() {
        columns.clear();
        while (table.getColumnCount() > 0) {
            table.removeColumn(0);
        }
    }

    @Override
    public TableHeader[] getCurrentTableHeaders() {
        if (taxType == TaxType.PROPERTY) {
            return tableHeaderProperty;
        } else if (taxType == TaxType.TRANSPORT) {
            return tableHeaderTransport;
        } else if (taxType == TaxType.INCOME) {
            return tableHeaderIncome;
        }
        return new TableHeader[0];
    }

    @Override
    public void setTableColumns(List<RefBookAttribute> attributes) {
        model.getList().clear();
        table.redraw();
        removeAllColumns();

        checkColumn = new Column<DataRow<Cell>, Boolean>(
                new CheckboxCell(false, true)) {
            @Override
            public Boolean getValue(DataRow<Cell> object) {
                return selectionModel.isSelected(object);
            }
        };

        checkColumn.setFieldUpdater(new FieldUpdater<DataRow<Cell>, Boolean>() {
            @Override
            public void update(int index, DataRow<Cell> object, Boolean chcked) {
                if (chcked) {
                    checkedRows.add(object);
                } else {
                    checkedRows.remove(object);
                }
                updateCheckBoxHeader(checkedRows.size() == table.getRowCount());
            }
        });

        table.setColumnWidth(checkColumn, 2, Style.Unit.EM);

        Map<String, RefBookAttribute> attributeMap = new HashMap<String, RefBookAttribute>();

        for (RefBookAttribute attr : attributes) {
            attributeMap.put(attr.getAlias(), attr);
        }
        for (TableHeader h : getCurrentTableHeaders()) {
            if (attributeMap.containsKey(h.name())) {
                RefBookAttribute cell = attributeMap.get(h.name());

                Column<DataRow<Cell>, ?> paramColumnUI = null;
                switch (cell.getAttributeType()) {
                    case STRING:
                        StringColumn textColumn = new StringColumn();
                        textColumn.setId(cell.getId().intValue());
                        textColumn.setAlias(h.name());
                        textColumn.setName(cell.getName());
                        textColumn.setMaxLength(cell.getMaxLength());
                        textColumn.setWidth(cell.getWidth());

                        paramColumnUI = factory.createTableColumn(textColumn, table);
                        table.setColumnWidth(paramColumnUI, cell.getWidth(), Style.Unit.EM);
                        table.addColumn(paramColumnUI, textColumn.getName());
                        columns.add(textColumn);
                        break;
                    case REFERENCE:
                        RefBookColumn refColumn = new RefBookColumn();
                        refColumn.setId(cell.getId().intValue());
                        refColumn.setRefBookAttributeId(cell.getRefBookAttributeId());
                        refColumn.setAlias(h.name());
                        refColumn.setName(cell.getName());
                        refColumn.setWidth(cell.getWidth());

                        paramColumnUI = factory.createTableColumn(refColumn, table);
                        table.setColumnWidth(paramColumnUI, cell.getWidth(), Style.Unit.EM);
                        table.addColumn(paramColumnUI, refColumn.getName());
                        columns.add(refColumn);
                        break;
                    case NUMBER:
                        NumericColumn numericColumn = new NumericColumn();
                        numericColumn.setId(cell.getId().intValue());
                        numericColumn.setAlias(h.name());
                        numericColumn.setName(cell.getName());
                        numericColumn.setWidth(cell.getWidth());
                        numericColumn.setMaxLength(cell.getMaxLength());
                        numericColumn.setPrecision(cell.getPrecision());

                        paramColumnUI = factory.createTableColumn(numericColumn, table);
                        table.setColumnWidth(paramColumnUI, cell.getWidth(), Style.Unit.EM);
                        table.addColumn(paramColumnUI, numericColumn.getName());
                        columns.add(numericColumn);
                        break;
                }
                if (paramColumnUI != null) {
                    ((DataRowColumn<?>) paramColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
                        @Override
                        public void onCellModified(CellModifiedEvent event, boolean withReference) {
                            if (getUiHandlers() != null) {
                                isFieldsModified = true;
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void setTextFieldsParams(List<RefBookAttribute> attributes) {
        for (RefBookAttribute attr : attributes) {
            if ("INN".equals(attr.getAlias())) {
                inn.setMaxLength(attr.getMaxLength());
            } else if ("FORMAT_VERSION".equals(attr.getAlias())) {
                formatVersion.setMaxLength(attr.getMaxLength());
            } else if ("PREPAYMENT_VERSION".equals(attr.getAlias())) {
                version.setMaxLength(attr.getMaxLength());
            }
        }
    }

    @Override
    public Map<String, TableCell> getNonTableParams() {
        Map<String, TableCell> params = new HashMap<String, TableCell>();
        TableCell innCell = new TableCell();
        innCell.setType(RefBookAttributeType.STRING);
        innCell.setStringValue(inn.getValue());
        params.put("INN", innCell);

        TableCell formatVersionCell = new TableCell();
        formatVersionCell.setType(RefBookAttributeType.STRING);
        formatVersionCell.setStringValue(formatVersion.getValue());
        params.put("FORMAT_VERSION", formatVersionCell);

        if (taxType == TaxType.PROPERTY) {
            TableCell versionCell = new TableCell();
            versionCell.setType(RefBookAttributeType.STRING);
            versionCell.setStringValue(version.getValue());
            params.put("PREPAYMENT_VERSION", versionCell);
        } else if (taxType == TaxType.INCOME) {
            TableCell taxRateCell = new TableCell();
            taxRateCell.setType(RefBookAttributeType.NUMBER);
            taxRateCell.setNumberValue(taxRate.getValue());
            params.put("TAX_RATE", taxRateCell);
        }

        return params;
    }

    @Override
    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
        initTable(taxType);
        versionBlock.setVisible(false);
        taxRateBlock.setVisible(false);
        if (taxType == TaxType.TRANSPORT) {
            taxTypeLbl.setText("Транспортный налог");
        } else if (taxType == TaxType.PROPERTY) {
            versionBlock.setVisible(true);
            taxTypeLbl.setText("Налог на имущество");
        } else if (taxType == TaxType.INCOME) {
            taxRateBlock.setVisible(true);
            taxTypeLbl.setText("Налог на прибыль");
        }
    }

    @Override
    public TaxType getTaxType() {
        return this.taxType;
    }

    @Override
    public void setTableData(int startIndex, long count, List<Map<String, TableCell>> itemList) {
        model.getList().clear();
        model.getList().addAll(makeDataRowsFromModel(itemList));
        table.redraw();
    }

    @Override
    public void fillNotTableData(Map<String, TableCell> itemList) {
        inn.setText(itemList.get("INN").getStringValue());
        formatVersion.setText(itemList.get("FORMAT_VERSION").getStringValue());
        if (taxType == TaxType.PROPERTY) {
            version.setText(itemList.get("PREPAYMENT_VERSION").getStringValue());
        } else if (taxType == TaxType.INCOME) {
            taxRate.setValue(itemList.get("TAX_RATE").getNumberValue() == null
                    ? null
                    : itemList.get("TAX_RATE").getNumberValue().doubleValue());
        }
    }

    @Override
    public void clearNonTableData() {
        inn.setText("");
        formatVersion.setText("");
        version.setText("");
        taxRate.setValue(null);
    }

    @Override
    public void clearTableData() {
        model.getList().clear();
        table.redraw();
    }

    @Override
    public List<DataRow<Cell>> getCheckedRows() {
        return checkedRows;
    }

    @Override
    public List<DataRow<Cell>> getTableRows() {
        return model.getList();
    }

    @UiHandler("delLink")
    public void onDeleteRow(ClickEvent event) {
        for (DataRow<Cell> row : getCheckedRows()) {
            model.getList().remove(row);
        }
        table.redraw();
        setIsFormModified(true);
    }

    @UiHandler("addLink")
    public void onAddRow(ClickEvent event) {
        model.getList().add(createDataRow());
        updateCheckBoxHeader(false);
        setIsFormModified(true);
    }

    private void updateCheckBoxHeader(boolean value) {
        if (table.getHeaderBuilder() instanceof TableWithCheckedColumn) {
            ((TableWithCheckedColumn) table.getHeaderBuilder()).getCheckBoxHeader().setValue(value);
        }
        table.redraw();
    }

    private DataRow<Cell> createDataRow() {
        DataRow<Cell> dataRow = new DataRow<Cell>();

        List<Cell> cells = new ArrayList<Cell>();

        for (com.aplana.sbrf.taxaccounting.model.Column col : columns) {
            Cell paramCell = new Cell();
            paramCell.setColumn(col);
            paramCell.setEditable(isEditMode);
            cells.add(paramCell);
        }

        dataRow.setFormColumns(cells);
        return dataRow;
    }

    private List<DataRow<Cell>> makeDataRowsFromModel(List<Map<String, TableCell>> itemList) {
        List<DataRow<Cell>> convertedRows = new ArrayList<DataRow<Cell>>();
        for (Map<String, TableCell> row : itemList) {
            DataRow<Cell> dataRow = new DataRow<Cell>();

            List<Cell> cells = new ArrayList<Cell>();

            for (com.aplana.sbrf.taxaccounting.model.Column col : columns) {
                Cell paramCell = new Cell();
                paramCell.setColumn(col);
                paramCell.setEditable(isEditMode);
                TableCell c = row.get(col.getAlias());
                if (c.getType() == RefBookAttributeType.STRING) {
                    paramCell.setStringValue(c.getStringValue());
                } else if (c.getType() == RefBookAttributeType.REFERENCE) {
                    paramCell.setNumericValue(c.getRefValue() == null ? null : new BigDecimal(c.getRefValue()));
                    paramCell.setRefBookDereference(c.getDeRefValue());
                } else if (c.getType() == RefBookAttributeType.NUMBER) {
                    paramCell.setNumericValue(c.getNumberValue() == null ? null : new BigDecimal(c.getNumberValue().longValue()));
                }
                cells.add(paramCell);
            }

            dataRow.setFormColumns(cells);
            convertedRows.add(dataRow);
        }

        return convertedRows;
    }

    @UiHandler("editButton")
    public void onEdit(ClickEvent event) {
        setEditMode(true);
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onCancel();
        }
    }

    @UiHandler("deleteButton")
    public void onDelete(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onDelete();
        }
    }


    @Override
    public void setEditMode(boolean isEditable) {
        isEditMode = isEditable;
        editButton.setVisible(!isEditMode);

        periodPickerPopup.setEnabled(!isEditMode);
        departmentPicker.setEnabled(!isEditMode);
        saveButton.setVisible(isEditMode);
        deleteButton.setVisible(isEditMode);
        cancelButton.setVisible(isEditMode);

        addLink.setVisible(isEditMode);
        delLink.setVisible(isEditMode);

        inn.setEnabled(isEditMode);
        version.setEnabled(isEditMode);
        taxRate.setEnabled(isEditMode);
        formatVersion.setEnabled(isEditMode);

        editModeLabel.setVisible(isEditMode);
        findButton.setVisible(!isEditMode);


        for (DataRow<Cell> row : model.getList()) {
            for (TableHeader h : getCurrentTableHeaders()) {
                try {
                    if (isEditable) {
                        if (isUnp != null && isUnp) {
                            if ("OBLIGATION".equals(h.name) || "TYPE".equals(h.name)) {
                                row.getCell(h.name()).setEditable(false);
                            } else {
                                row.getCell(h.name()).setEditable(true);
                            }
                        } else {
                            row.getCell(h.name()).setEditable(true);
                        }
                    } else {
                        row.getCell(h.name()).setEditable(false);
                    }
                } catch (IllegalArgumentException ex) {

                }
            }
        }

        if (table.getHeaderBuilder() instanceof TableWithCheckedColumn) {
            if (!isEditable) {
                if (table.getColumnIndex(checkColumn) != -1) {
                    table.removeColumn(checkColumn);
                }
            } else {
                table.insertColumn(0, checkColumn);
            }
            ((TableWithCheckedColumn) table.getHeaderBuilder()).setNeedCheckedRow(isEditable);
            table.getHeaderBuilder().buildHeader();
        }
        table.redraw();
        ellipsizeDepartmentPickerLabel(!isEditMode);
    }

    @Override
    public boolean isFormModified() {
        return isFieldsModified;
    }

    @Override
    public void setIsFormModified(boolean isModified) {
        this.isFieldsModified = isModified;
    }

    @UiHandler("saveButton")
    public void onSave(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onSave();
        }
    }

    @UiHandler("findButton")
    public void onFind(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onFind();
        }
    }

    @Override
    public void setDepartment(final Department department) {
        if (department != null) {
            departmentPicker.setValue(Arrays.asList(department.getId()), true);
        }

        this.currentDepartmentId = department != null ? department.getId() : null;
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartments) {
        departmentPicker.setAvalibleValues(departments, availableDepartments);
        departmentPicker.setValue(Arrays.asList(departments.get(0).getId()), true);
    }

    @Override
    public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        periodPickerPopup.setPeriods(reportPeriods);
        ReportPeriod maxPeriod = getMaxPeriod(reportPeriods);
        periodPickerPopup.setValue(maxPeriod == null ? null : Arrays.asList(maxPeriod.getId()), true);
        this.currentReportPeriodId = maxPeriod == null ? null : maxPeriod.getId();

    }

    private ReportPeriod getMaxPeriod(List<ReportPeriod> reportPeriods) {
        if (reportPeriods == null || reportPeriods.isEmpty()) {
            return null;
        }
        ReportPeriod maxPeriod = reportPeriods.get(0);
        for (ReportPeriod per : reportPeriods) {
            if (per.getCalendarStartDate().after(maxPeriod.getCalendarStartDate())) {
                maxPeriod = per;
            }
        }
        return maxPeriod;
    }

    @Override
    public Integer getDepartmentId() {
        return departmentPicker.getSingleValue();
    }

    @Override
    public Integer getReportPeriodId() {
        if (periodPickerPopup.getValue().isEmpty())
            return null;
        return periodPickerPopup.getValue().get(0);
    }

    @Override
    public RefBookColumn getParamColumn() {
        return paramColumn;
    }

    @Override
    public void setData(List<DataRow<Cell>> data) {
        table.setRowData(0, data);
    }

    @Override
    public StringColumn getTextColumn() {
        return null;
    }

    @Override
    public void removeResizeHandler() {
        if (resizeHandler != null) {
            resizeHandler.removeHandler();
        }
    }

    @Override
    public void addResizeHandler() {
        if (resizeHandler == null)
            resizeHandler = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    resizeTimer.scheduleRepeating(5);
                }
            });
    }

    @Override
    public void setIsUnp(boolean isUnp) {
        this.isUnp = isUnp;
    }

    @Override
    public void updateVisibleEditButton() {
        editButton.setEnabled(!isEditMode);
    }

    @Override
    public void setRefBookPeriod(Date startDate, Date endDate) {
        factory.setDateRange(startDate, endDate);
        getUiHandlers().createTableColumns();
    }

    /**
     * Вручную выставляем многоточние в конце, если текст занимает больше 2х строк
     *
     * @param enabled
     */
    private void ellipsizeDepartmentPickerLabel(boolean enabled) {
        if (!enabled) {
            String text = departmentPicker.getText();
            Element el = departmentPicker.getLabel().getElement();
            el.setInnerText(text);
            for (; el.getScrollHeight() > 32; ) {
                if (!text.isEmpty()) {
                    text = text.substring(0, text.length() - 1);
                    el.setInnerText(text + "…");
                } else {
                    break;
                }
            }
        }
    }
}