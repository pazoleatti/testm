package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.configuration.client.ConfigurationPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers> implements MyView {
    @UiField
    DataGrid<DataRow<Cell>> commonTable, formTable;

    List<DataRow<Cell>> formRowsData;
    List<DataRow<Cell>> commonRowsData;

    private RefBookColumn paramColumn = new RefBookColumn();
    private StringColumn valueColumn = new StringColumn();

    private RefBookColumn departmentColumn = new RefBookColumn();
    private StringColumn uploadPathColumn = new StringColumn();
    private StringColumn archivePathColumn = new StringColumn();
    private StringColumn errorPathColumn = new StringColumn();

    @UiField
    Widget commonLink, formLink, commonPanel, formPanel;

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    interface Binder extends UiBinder<Widget, ConfigurationView> {
	}

	@Inject
	public ConfigurationView(final Binder binder) {
		initWidget(binder.createAndBindUi(this));
        initCommonTable();
        initFormTable();
	}

    /**
     * Подготовка таблицы общих параметров
     */
    private void initCommonTable() {
        paramColumn.setId(1);
        paramColumn.setRefBookAttributeId(1041L);
        // Допустимые значения
        String filter = "";
        for (ConfigurationParam param : ConfigurationParam.values()) {
            if (param.isCommon()) {
                filter += param.name() + ",";
            }
        }
        filter = filter.substring(0, filter.length() - 1);
        paramColumn.setFilter(filter);
        paramColumn.setAlias("paramColumn");
        paramColumn.setName("Параметр");
        Column<DataRow<Cell>, ?> paramColumnUI = factory.createTableColumn(paramColumn, commonTable);
        commonTable.setColumnWidth(paramColumnUI, 50, Style.Unit.EM);
        commonTable.addColumn(paramColumnUI, paramColumn.getName());

        valueColumn.setAlias("valueColumn");
        valueColumn.setName("Значение параметра");
        commonTable.addColumn(factory.createTableColumn(valueColumn, commonTable), valueColumn.getName());

        commonTable.setRowData(0, new ArrayList<DataRow<Cell>>(0));
        commonTable.setMinimumTableWidth(70, Style.Unit.EM);
    }

    /**
     * Подготовка таблицы параметров загрузки НФ
     */
    private void initFormTable() {
        departmentColumn.setId(1);
        departmentColumn.setNameAttributeId(161L);
        departmentColumn.setRefBookAttributeId(161L);
        departmentColumn.setHierarchical(true);
        departmentColumn.setAlias("departmentColumn");
        departmentColumn.setName("Наименование ТБ");
        departmentColumn.setWidth(1);
        Column<DataRow<Cell>, ?> departmentColumnUI = factory.createTableColumn(departmentColumn, formTable);
        formTable.setColumnWidth(departmentColumnUI, 20, Style.Unit.EM);
        formTable.addColumn(departmentColumnUI, departmentColumn.getName());

        uploadPathColumn.setAlias("uploadPathColumn");
        uploadPathColumn.setName("Путь к каталогу загрузки");
        formTable.addColumn(factory.createTableColumn(uploadPathColumn, formTable), uploadPathColumn.getName());

        archivePathColumn.setAlias("archivePathColumn");
        archivePathColumn.setName("Путь к каталогу aрхива");
        formTable.addColumn(factory.createTableColumn(archivePathColumn, formTable), archivePathColumn.getName());

        errorPathColumn.setAlias("errorPathColumn");
        errorPathColumn.setName("Путь к каталогу ошибок");
        formTable.addColumn(factory.createTableColumn(errorPathColumn, formTable), errorPathColumn.getName());
        formTable.setRowData(0, new ArrayList<DataRow<Cell>>(0));
        formTable.setMinimumTableWidth(70, Style.Unit.EM);
    }

    // Переключение между вкладками
    private void showTab(int index) {
        commonLink.setVisible(index == 1);
        commonPanel.setVisible(index == 0);
        formLink.setVisible(index == 0);
        formPanel.setVisible(index == 1);
        commonTable.redraw();
        formTable.redraw();
    }

    @UiHandler("addLink")
    void onAddLinkClick(ClickEvent event) {
        if (commonPanel.isVisible()) {
            getUiHandlers().onCommonAddRow();
        } else {
            getUiHandlers().onFormAddRow();
        }
    }

    @UiHandler("delLink")
    void onDelLinkClick(ClickEvent event) {
        if (commonPanel.isVisible() && commonRowsData != null && commonTable.getKeyboardSelectedRow() < commonRowsData.size()) {
            commonRowsData.remove(commonRowsData.get(commonTable.getKeyboardSelectedRow()));
            setCommonConfigData(commonRowsData);
        } else if (formRowsData != null && formTable.getKeyboardSelectedRow() < formRowsData.size())  {
            formRowsData.remove(formRowsData.get(formTable.getKeyboardSelectedRow()));
            setFormConfigData(formRowsData);
        }
    }

    @UiHandler("commonLink")
    void onCommonLinkClick(ClickEvent event) {
        showTab(0);
    }

    @UiHandler("formLink")
    void onFormLinkClick(ClickEvent event) {
        showTab(1);
    }
	
	@UiHandler("saveButton")
	void onSaveButtonClick(ClickEvent event) {
		getUiHandlers().onSave();
	}

	@UiHandler("cancelButton")
	void onCancelButtonClick(ClickEvent event) {
		getUiHandlers().onCancel();
	}

    @Override
    public void setCommonConfigData(List<DataRow<Cell>> rowsData) {
        commonRowsData = rowsData;
        commonTable.setVisibleRange(new Range(0, rowsData.size()));
        commonTable.setRowCount(rowsData.size());
        commonTable.setRowData(0, rowsData);
    }

	@Override
	public void setFormConfigData(List<DataRow<Cell>> rowsData) {
        formRowsData = rowsData;
        formTable.setVisibleRange(new Range(0, rowsData.size()));
        formTable.setRowCount(rowsData.size());
        formTable.setRowData(0, rowsData);
	}

    @Override
    public RefBookColumn getParamColumn() {
        return paramColumn;
    }

    @Override
    public StringColumn getValueColumn() {
        return valueColumn;
    }

    @Override
    public RefBookColumn getDepartmentColumn() {
        return departmentColumn;
    }

    @Override
    public StringColumn getUploadPathColumn() {
        return uploadPathColumn;
    }

    @Override
    public StringColumn getArchivePathColumn() {
        return archivePathColumn;
    }

    @Override
    public StringColumn getErrorPathColumn() {
        return errorPathColumn;
    }

    @Override
    public List<DataRow<Cell>> getFormRowsData() {
        return formRowsData;
    }

    @Override
    public List<DataRow<Cell>> getCommonRowsData() {
        return commonRowsData;
    }
}
