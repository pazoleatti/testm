package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client;

import com.aplana.gwt.client.TextBox;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.AsyncDataProviderWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.ViewWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.AuditClientUIHandler;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.TableCell;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.TableRow;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomHeaderBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.EditTextColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.ReferenceUiColumn;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

import static java.util.Arrays.asList;

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

    public static enum TABLE_HEADER {
        TAX_ORGAN_CODE,
        KPP,
        TAX_PLACE_TYPE_CODE,
        NAME,
        OKVED_CODE,
        PHONE,
        REORG_FORM_CODE,
        REORG_INN,
        REORG_KPP,
        SIGNATORY_ID,
        SIGNATORY_SURNAME,
        SIGNATORY_FIRSTNAME,
        SIGNATORY_LASTNAME,
        APPROVE_DOC_NAME,
        APPROVE_ORG_NAME;
    }

    interface Binder extends UiBinder<Widget, DepartmentConfigPropertyView> {
	}

    private AsyncDataProviderWithSortableTable dataProvider;
    @UiField
    DataGrid<DataRow<Cell>> table;
    @UiField
    TextBox inn;
    @UiField
    TextBox formatVersion;
    @UiField
    TextBox  version;
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
    Label editModeLabel;
    @UiField
    Button findButton;

    private Integer currentDepartmentId;

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

    boolean isEditMode = false;

    @Inject
	@UiConstructor
	public DepartmentConfigPropertyView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        ConstHeaderBuilder hb = new ConstHeaderBuilder(table);
        hb.setNeedCheckedRow(false);
        table.setHeaderBuilder(hb);
        table.setSelectionModel(selectionModel);


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
    public void setTableColumns(List<RefBookAttribute> attributes) {
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
            }
        });

        table.setColumnWidth(checkColumn, 5, Style.Unit.PX);

        Map<String, RefBookAttribute> attributeMap = new HashMap<String, RefBookAttribute>();

        for (RefBookAttribute attr : attributes) {
            attributeMap.put(attr.getAlias(), attr);
        }

        for (TABLE_HEADER h : TABLE_HEADER.values()) {
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
                        table.setColumnWidth(paramColumnUI, cell.getWidth(), Style.Unit.PX);
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
                        table.setColumnWidth(paramColumnUI, cell.getWidth(), Style.Unit.PX);
                        table.addColumn(paramColumnUI, refColumn.getName());
                        columns.add(refColumn);
                        break;
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

        TableCell versionCell = new TableCell();
        versionCell.setType(RefBookAttributeType.STRING);
        versionCell.setStringValue(version.getValue());
        params.put("PREPAYMENT_VERSION", versionCell);

        return params;
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
        version.setText(itemList.get("PREPAYMENT_VERSION").getStringValue());
    }

    @Override
    public void clearNonTableData() {
        inn.setText("");
        formatVersion.setText("");
        version.setText("");
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
    }

    @UiHandler("addLink")
    public void onAddRow(ClickEvent event) {
        model.getList().add(createDataRow());
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
                } else if (c.getType() == RefBookAttributeType.REFERENCE){
                    paramCell.setRefBookDereference(c.getDeRefValue());
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
        setEditMode(false);
    }

    @UiHandler("deleteButton")
    public void onDelete(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onDelete();
        }
    }


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
        formatVersion.setEnabled(isEditMode);

        editModeLabel.setVisible(isEditMode);
        findButton.setVisible(!isEditMode);


        for (DataRow<Cell> row : model.getList()) {
            for (TABLE_HEADER h : TABLE_HEADER.values()) {
                row.getCell(h.name()).setEditable(isEditMode);
            }
        }

        if (table.getHeaderBuilder() instanceof ConstHeaderBuilder) {
            if (!isEditable) {
                table.removeColumn(checkColumn);
            } else {
                table.insertColumn(0, checkColumn);
            }
            ((ConstHeaderBuilder)table.getHeaderBuilder()).setNeedCheckedRow(isEditable);
            table.getHeaderBuilder().buildHeader();
        }
        table.redraw();
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
        departmentPicker.setValue(Arrays.asList(departments.get(0).getId()));
    }

    @Override
    public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        periodPickerPopup.setPeriods(reportPeriods);
        periodPickerPopup.setValue(Arrays.asList(reportPeriods.get(0).getId()));
    }

    @Override
    public Integer getDepartmentId() {
        return departmentPicker.getSingleValue();
    }

    @Override
    public Integer getReportPeriodId() {
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
}