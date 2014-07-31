package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.configuration.client.ConfigurationPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers> implements MyView {
    @UiField
    DataGrid<DataRow<Cell>> commonTable, formTable;

    @UiField
    Label titleLabel;

    @UiField
    LinkAnchor formCommonLink;

    @UiField
    Widget commonPanel, formPanel;

    List<DataRow<Cell>> formRowsData;
    List<DataRow<Cell>> commonRowsData;

    private RefBookColumn paramColumn = new RefBookColumn();
    private StringColumn valueColumn = new StringColumn();

    private RefBookColumn departmentColumn = new RefBookColumn();
    private StringColumn uploadPathColumn = new StringColumn();
    private StringColumn archivePathColumn = new StringColumn();
    private StringColumn errorPathColumn = new StringColumn();

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
        commonTable.setColumnWidth(paramColumnUI, 30, Style.Unit.EM);
        commonTable.addColumn(paramColumnUI, paramColumn.getName());

        valueColumn.setAlias("valueColumn");
        valueColumn.setName("Значение параметра");
        commonTable.addColumn(factory.createTableColumn(valueColumn, commonTable), valueColumn.getName());

        commonTable.setRowData(0, new ArrayList<DataRow<Cell>>(0));
        commonTable.setMinimumTableWidth(40, Style.Unit.EM);

        commonTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
        SingleSelectionModel<DataRow<Cell>> singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();
        commonTable.setSelectionModel(singleSelectionModel);

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>)paramColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    commonTable.redraw();
                }
            }
        });
    }

    /**
     * Подготовка таблицы параметров загрузки НФ
     */
    private void initFormTable() {
        departmentColumn.setId(1);
        departmentColumn.setNameAttributeId(161L);
        departmentColumn.setRefBookAttributeId(161L);
        departmentColumn.setHierarchical(true);
        departmentColumn.setFilter("TYPE=2"); // ТБ
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

        formTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
        SingleSelectionModel<DataRow<Cell>> singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();
        formTable.setSelectionModel(singleSelectionModel);

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>)departmentColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    formTable.redraw();
                }
            }
        });
    }

    // Переключение между вкладками
    private void showTab(int index) {
        titleLabel.setText(formCommonLink.getText());
        formCommonLink.setText(index == 1 ? "Общие параметры" : "Параметры загрузки налоговых форм");
        commonPanel.setVisible(index == 0);
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
        DataGrid<DataRow<Cell>> table = commonPanel.isVisible() ? commonTable : formTable;
        List<DataRow<Cell>> rowsData = commonPanel.isVisible() ? commonRowsData : formRowsData;
        DataRow<Cell> selRow = ((SingleSelectionModel<DataRow<Cell>>) table.getSelectionModel()).getSelectedObject();
        if (selRow == null) {
            return;
        }
        rowsData.remove(selRow);
        if (rowsData == commonRowsData) {
            setCommonConfigData(rowsData, false);
        } else {
            setFormConfigData(rowsData, false);
        }
        ((SingleSelectionModel<DataRow<Cell>>) table.getSelectionModel()).setSelected(selRow, false);
    }

    @UiHandler("formCommonLink")
    void onFormCommonLinkClick(ClickEvent event) {
        showTab(commonPanel.isVisible() ? 1 : 0);
    }

    @UiHandler("checkButton")
    void onCheckButtonClick(ClickEvent event) {
        DataGrid<DataRow<Cell>> table = commonPanel.isVisible() ? commonTable : formTable;
        DataRow<Cell> selRow = ((SingleSelectionModel<DataRow<Cell>>) table.getSelectionModel()).getSelectedObject();
        if (selRow == null) {
            return;
        }
        getUiHandlers().onCheckReadWriteAccess(selRow, commonPanel.isVisible());
    }

    @UiHandler("saveButton")
	void onSaveButtonClick(ClickEvent event) {
		getUiHandlers().onSave();
	}

	@UiHandler("cancelButton")
	void onCancelButtonClick(ClickEvent event) {
		getUiHandlers().onCancel();
    }

    private static final Comparator<DataRow<Cell>> commonComparator = new Comparator<DataRow<Cell>>() {
        @Override
        public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
            String name1 = o1.getCell("paramColumn").getRefBookDereference();
            String name2 = o2.getCell("paramColumn").getRefBookDereference();
            if (name1 == null && name2 == null) {
                return 0;
            }
            if (name1 == null) {
                return 1;
            }
            if (name2 == null) {
                return -1;
            }
            return name1.compareTo(name2);
        }
    };

    private static final Comparator<DataRow<Cell>> formComparator = new Comparator<DataRow<Cell>>() {
        @Override
        public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
            String name1 = o1.getCell("departmentColumn").getRefBookDereference();
            String name2 = o2.getCell("departmentColumn").getRefBookDereference();
            if (name1 == null && name2 == null) {
                return 0;
            }
            if (name1 == null) {
                return 1;
            }
            if (name2 == null) {
                return -1;
            }
            return name1.compareTo(name2);
        }
    };

    @Override
    public void setCommonConfigData(List<DataRow<Cell>> rowsData) {
        setCommonConfigData(rowsData, true);
    }

    private void setCommonConfigData(List<DataRow<Cell>> rowsData, boolean needSort) {
        if (needSort) {
            Collections.sort(rowsData, commonComparator);
        }
        commonRowsData = rowsData;
        commonTable.setVisibleRange(new Range(0, rowsData.size()));
        commonTable.setRowCount(rowsData.size());
        commonTable.setRowData(0, rowsData);
    }

	@Override
	public void setFormConfigData(List<DataRow<Cell>> rowsData) {
        setFormConfigData(rowsData, true);
	}

    private void setFormConfigData(List<DataRow<Cell>> rowsData, boolean needSort) {
        if (needSort) {
            Collections.sort(rowsData, formComparator);
        }
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

    @Override
    public void clearSelection() {
        ((SingleSelectionModel)commonTable.getSelectionModel()).clear();
        ((SingleSelectionModel)formTable.getSelectionModel()).clear();
    }
}
