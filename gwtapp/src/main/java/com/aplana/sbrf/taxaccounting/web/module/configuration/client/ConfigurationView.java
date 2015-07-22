package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.configuration.client.ConfigurationPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers> implements MyView {
    @UiField
    GenericDataGrid<DataRow<Cell>> commonTable, formTable, emailTable, asyncTable;

    @UiField
    Label titleLabel;

    @UiField
    LinkButton commonLink, formLink, emailLink, asyncLink;

    @UiField
    Widget commonPanel, formPanel, emailPanel, asyncPanel;

    @UiField
    LinkButton addLink;

    @UiField
    LinkButton delLink;

    @UiField
    Button checkButton;

    List<DataRow<Cell>> formRowsData = new ArrayList<DataRow<Cell>>();
    List<DataRow<Cell>> commonRowsData = new ArrayList<DataRow<Cell>>();
    List<DataRow<Cell>> emailRowsData = new ArrayList<DataRow<Cell>>();
    List<DataRow<Cell>> asyncRowsData = new ArrayList<DataRow<Cell>>();

    private RefBookColumn paramColumn = new RefBookColumn();
    private StringColumn valueColumn = new StringColumn();

    private RefBookColumn departmentColumn = new RefBookColumn();
    private StringColumn uploadPathColumn = new StringColumn();
    private StringColumn archivePathColumn = new StringColumn();
    private StringColumn errorPathColumn = new StringColumn();

    private StringColumn emailNameColumn = new StringColumn();
    private StringColumn emailValueColumn = new StringColumn();
    private StringColumn emailDescriptionColumn = new StringColumn();

    private StringColumn asyncTypeIdColumn = new StringColumn();
    private StringColumn asyncTypeColumn = new StringColumn();
    private StringColumn asyncLimitKindColumn = new StringColumn();
    private StringColumn asyncLimitColumn = new StringColumn();
    private StringColumn asyncShortLimitColumn = new StringColumn();

    private ConfigurationParamGroup activeGroup = ConfigurationParamGroup.COMMON;

    private final Map<ConfigurationParamGroup, DataGrid<DataRow<Cell>>> tableMap = new HashMap<ConfigurationParamGroup, DataGrid<DataRow<Cell>>>();
    private final Map<ConfigurationParamGroup, List<DataRow<Cell>>> rowsDataMap = new HashMap<ConfigurationParamGroup, List<DataRow<Cell>>>();
    private final Map<ConfigurationParamGroup, Comparator<DataRow<Cell>>> comparatorMap = new HashMap<ConfigurationParamGroup, Comparator<DataRow<Cell>>>();

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    interface Binder extends UiBinder<Widget, ConfigurationView> {
    }

    @Inject
    public ConfigurationView(final Binder binder) {
        initWidget(binder.createAndBindUi(this));
        initMaps();
    }

    private void initMaps() {
        tableMap.put(ConfigurationParamGroup.COMMON, commonTable);
        tableMap.put(ConfigurationParamGroup.FORM, formTable);
        tableMap.put(ConfigurationParamGroup.EMAIL, emailTable);
        tableMap.put(ConfigurationParamGroup.ASYNC, asyncTable);

        rowsDataMap.put(ConfigurationParamGroup.COMMON, commonRowsData);
        rowsDataMap.put(ConfigurationParamGroup.FORM, formRowsData);
        rowsDataMap.put(ConfigurationParamGroup.EMAIL, emailRowsData);
        rowsDataMap.put(ConfigurationParamGroup.ASYNC, asyncRowsData);

        comparatorMap.put(ConfigurationParamGroup.COMMON, commonComparator);
        comparatorMap.put(ConfigurationParamGroup.FORM, formComparator);
        comparatorMap.put(ConfigurationParamGroup.ASYNC, asyncComparator);
    }

    /**
     * Подготовка таблицы общих параметров
     */
    private void initCommonTable() {
        paramColumn.setId(1);
        paramColumn.setRefBookAttributeId(1041L);
        // Допустимые значения
        paramColumn.setFilter(getConfParamFilter(ConfigurationParamGroup.COMMON));
        paramColumn.setAlias("paramColumn");
        paramColumn.setName("Параметр");
        paramColumn.setSearchEnabled(false);
        Column<DataRow<Cell>, ?> paramColumnUI = factory.createTableColumn(paramColumn, commonTable);
        commonTable.setColumnWidth(paramColumnUI, 30, Style.Unit.EM);
        commonTable.addColumn(paramColumnUI, paramColumn.getName());

        valueColumn.setAlias("valueColumn");
        valueColumn.setName("Значение параметра");
        commonTable.addColumn(factory.createTableColumn(valueColumn, commonTable), valueColumn.getName());

        commonTable.setRowData(0, new ArrayList<DataRow<Cell>>(0));
        commonTable.setMinimumTableWidth(40, Style.Unit.EM);

        commonTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
        final SingleSelectionModel<DataRow<Cell>> singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();
        commonTable.setSelectionModel(singleSelectionModel);
        singleSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (commonPanel.isVisible()){
                    enableButtons(singleSelectionModel.getSelectedObject() != null);
                }
            }
        });

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>) paramColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
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
        final SingleSelectionModel<DataRow<Cell>> singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();
        formTable.setSelectionModel(singleSelectionModel);
        singleSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (formPanel.isVisible()){
                    enableButtons(singleSelectionModel.getSelectedObject() != null);
                }
            }
        });

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>) departmentColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    formTable.redraw();
                }
            }
        });
    }

    /**
     * Подготовка таблицы параметров электронной почты
     */
    private void initEmailTable() {
        emailNameColumn.setAlias("emailNameColumn");
        emailNameColumn.setName("Параметр");
        Column<DataRow<Cell>, ?> emailParamColumnUI = factory.createTableColumn(emailNameColumn, emailTable);
        emailTable.setColumnWidth(emailParamColumnUI, 20, Style.Unit.EM);
        emailTable.addColumn(emailParamColumnUI, emailNameColumn.getName());

        emailValueColumn.setAlias("emailValueColumn");
        emailValueColumn.setName("Значение параметра");
        emailParamColumnUI = factory.createTableColumn(emailValueColumn, emailTable);
        emailTable.setColumnWidth(emailParamColumnUI, 30, Style.Unit.EM);
        emailTable.addColumn(emailParamColumnUI, emailValueColumn.getName());

        emailDescriptionColumn.setAlias("emailCodeColumn");
        emailDescriptionColumn.setName("Описание");
        emailTable.addColumn(factory.createTableColumn(emailDescriptionColumn, emailTable), emailDescriptionColumn.getName());

        emailTable.setRowData(0, new ArrayList<DataRow<Cell>>(0));
        emailTable.setMinimumTableWidth(20, Style.Unit.EM);

        emailTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
        SingleSelectionModel<DataRow<Cell>> singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();
        emailTable.setSelectionModel(singleSelectionModel);

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>) emailParamColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    emailTable.redraw();
                }
            }
        });
    }

    /**
     * Подготовка таблицы параметров электронной почты
     */
    private void initAsyncTable() {
        asyncTypeIdColumn.setAlias("asyncTypeIdColumn");
        asyncTypeIdColumn.setName("");
        Column<DataRow<Cell>, ?> asyncParamColumnUI = factory.createTableColumn(asyncTypeIdColumn, asyncTable);
        asyncTable.setColumnWidth(asyncParamColumnUI, 0, Style.Unit.EM);
        asyncTable.addColumn(asyncParamColumnUI, asyncTypeIdColumn.getName());

        asyncTypeColumn.setAlias("asyncTypeColumn");
        asyncTypeColumn.setName("Тип задания");
        asyncParamColumnUI = factory.createTableColumn(asyncTypeColumn, asyncTable);
        asyncTable.setColumnWidth(asyncParamColumnUI, 20, Style.Unit.EM);
        asyncTable.addColumn(asyncParamColumnUI, asyncTypeColumn.getName());

        asyncLimitKindColumn.setAlias("asyncLimitKindColumn");
        asyncLimitKindColumn.setName("Вид ограничения");
        asyncParamColumnUI = factory.createTableColumn(asyncLimitKindColumn, asyncTable);
        asyncTable.setColumnWidth(asyncParamColumnUI, 20, Style.Unit.EM);
        asyncTable.addColumn(asyncParamColumnUI, asyncLimitKindColumn.getName());

        asyncLimitColumn.setAlias("asyncLimitColumn");
        asyncLimitColumn.setName("Значение параметра \"Ограничение на выполнение задания\"");
        asyncParamColumnUI = factory.createTableColumn(asyncLimitColumn, asyncTable);
        asyncTable.setColumnWidth(asyncParamColumnUI, 20, Style.Unit.EM);
        asyncTable.addColumn(asyncParamColumnUI, asyncLimitColumn.getName());

        asyncShortLimitColumn.setAlias("asyncShortLimitColumn");
        asyncShortLimitColumn.setName("Значение параметра \"Ограничение на выполнение задания в очереди быстрых заданий\"");
        asyncParamColumnUI = factory.createTableColumn(asyncShortLimitColumn, asyncTable);
        asyncTable.setColumnWidth(asyncParamColumnUI, 20, Style.Unit.EM);
        asyncTable.addColumn(asyncParamColumnUI, asyncShortLimitColumn.getName());

        asyncTable.setRowData(0, new ArrayList<DataRow<Cell>>(0));
        asyncTable.setMinimumTableWidth(20, Style.Unit.EM);

        asyncTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
        SingleSelectionModel<DataRow<Cell>> singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();
        asyncTable.setSelectionModel(singleSelectionModel);

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>) asyncParamColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    asyncTable.redraw();
                }
            }
        });
    }

    // Допустимые значения для справочника конф. параметров
    private String getConfParamFilter(ConfigurationParamGroup group) {
        StringBuilder filter = new StringBuilder("");
        for (ConfigurationParam param : ConfigurationParam.values()) {
            if (param.getGroup().equals(group)) {
                filter.append(param.name()).append(",");
            }
        }
        return filter.substring(0, filter.length() - 1);
    }

    // Переключение между вкладками
    private void showTab(ConfigurationParamGroup group) {
        activeGroup = group;

        titleLabel.setText(activeGroup.getCaption());

        commonLink.setVisible(!activeGroup.equals(ConfigurationParamGroup.COMMON));
        commonPanel.setVisible(activeGroup.equals(ConfigurationParamGroup.COMMON));

        formLink.setVisible(!activeGroup.equals(ConfigurationParamGroup.FORM));
        formPanel.setVisible(activeGroup.equals(ConfigurationParamGroup.FORM));

        emailLink.setVisible(!activeGroup.equals(ConfigurationParamGroup.EMAIL));
        emailPanel.setVisible(activeGroup.equals(ConfigurationParamGroup.EMAIL));

        asyncLink.setVisible(!activeGroup.equals(ConfigurationParamGroup.ASYNC));
        asyncPanel.setVisible(activeGroup.equals(ConfigurationParamGroup.ASYNC));

        addLink.setVisible(!activeGroup.equals(ConfigurationParamGroup.EMAIL) && !activeGroup.equals(ConfigurationParamGroup.ASYNC));
        delLink.setVisible(!activeGroup.equals(ConfigurationParamGroup.EMAIL) && !activeGroup.equals(ConfigurationParamGroup.ASYNC));

        for (DataGrid<DataRow<Cell>> table : tableMap.values()) {
            table.redraw();
        }
    }

    @UiHandler("addLink")
    void onAddLinkClick(ClickEvent event) {
        getUiHandlers().onAddRow(activeGroup, getSelectedIndex());
    }

    private DataGrid<DataRow<Cell>> getTable(ConfigurationParamGroup group) {
        return tableMap.get(group);
    }

    @Override
    public List<DataRow<Cell>> getRowsData(ConfigurationParamGroup group) {
        return rowsDataMap.get(group);
    }

    private Integer getSelectedIndex() {
        DataRow<Cell> selRow = ((SingleSelectionModel<DataRow<Cell>>) getTable(activeGroup).getSelectionModel()).getSelectedObject();
        if (selRow == null) {
            return null;
        }
        List<DataRow<Cell>> rowsData = getRowsData(activeGroup);
        return rowsData.indexOf(selRow);
    }

    @UiHandler("delLink")
    void onDelLinkClick(ClickEvent event) {
        DataGrid<DataRow<Cell>> table = getTable(activeGroup);
        DataRow<Cell> selRow = ((SingleSelectionModel<DataRow<Cell>>) table.getSelectionModel()).getSelectedObject();
        if (selRow == null) {
            return;
        }
        List<DataRow<Cell>> rowsData = new ArrayList<DataRow<Cell>>(getRowsData(activeGroup));
        rowsData.remove(selRow);
        ((SingleSelectionModel<DataRow<Cell>>) table.getSelectionModel()).clear();
        setConfigData(activeGroup, rowsData, false);
    }

    @UiHandler("commonLink")
    void onCommonLinkClick(ClickEvent event) {
        showTab(ConfigurationParamGroup.COMMON);
        enableButtons(((SingleSelectionModel)commonTable.getSelectionModel()).getSelectedObject() != null);
    }

    @UiHandler("formLink")
    void onFormLinkClick(ClickEvent event) {
        showTab(ConfigurationParamGroup.FORM);
        enableButtons(((SingleSelectionModel) formTable.getSelectionModel()).getSelectedObject() != null);
    }

    @UiHandler("emailLink")
    void onEmailLinkClick(ClickEvent event) {
        showTab(ConfigurationParamGroup.EMAIL);
    }

    @UiHandler("asyncLink")
    void onAsyncLinkClick(ClickEvent event) {
        showTab(ConfigurationParamGroup.ASYNC);
    }

    @UiHandler("checkButton")
    void onCheckButtonClick(ClickEvent event) {
        DataGrid<DataRow<Cell>> table = getTable(activeGroup);
        DataRow<Cell> selRow = ((SingleSelectionModel<DataRow<Cell>>) table.getSelectionModel()).getSelectedObject();
        if (selRow == null && !(activeGroup.equals(ConfigurationParamGroup.EMAIL) || activeGroup.equals(ConfigurationParamGroup.ASYNC))) {
            return;
        }
        getUiHandlers().onCheckAccess(activeGroup, selRow, false);
    }

    @UiHandler("saveButton")
    void onSaveButtonClick(ClickEvent event) {
        if (activeGroup.equals(ConfigurationParamGroup.ASYNC)) {
            getUiHandlers().onCheckAccess(activeGroup, null, true);
        } else {
            getUiHandlers().onSave();
        }
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

    private static final Comparator<DataRow<Cell>> asyncComparator = new Comparator<DataRow<Cell>>() {
        @Override
        public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
            String type1 = o1.getCell("asyncTypeColumn").getStringValue();
            String type2 = o2.getCell("asyncTypeColumn").getStringValue();
            if (type1 == null && type2 == null) {
                return 0;
            }
            if (type1 == null) {
                return 1;
            }
            if (type2 == null) {
                return -1;
            }
            return type1.compareTo(type2);
        }
    };

    @Override
    public void setConfigData(ConfigurationParamGroup group, List<DataRow<Cell>> rowsData, boolean needSort) {
        if (needSort) {
            Collections.sort(rowsData, comparatorMap.get(group));
        }

        getRowsData(group).clear();
        getRowsData(group).addAll(rowsData);

        getTable(group).setVisibleRange(new Range(0, rowsData.size()));
        getTable(group).setRowCount(rowsData.size());
        getTable(group).setRowData(0, rowsData);
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
    public StringColumn getEmailNameColumn() {
        return emailNameColumn;
    }

    @Override
    public StringColumn getEmailValueColumn() {
        return emailValueColumn;
    }

    @Override
    public StringColumn getEmailDescriptionColumn() {
        return emailDescriptionColumn;
    }

    @Override
    public StringColumn getAsyncTypeColumn() {
        return asyncTypeColumn;
    }

    @Override
    public StringColumn getAsyncLimitKindColumn() {
        return asyncLimitKindColumn;
    }

    @Override
    public StringColumn getAsyncLimitColumn() {
        return asyncLimitColumn;
    }

    @Override
    public StringColumn getAsyncShortLimitColumn() {
        return asyncShortLimitColumn;
    }

    @Override
    public void clearSelection() {
        for (DataGrid<DataRow<Cell>> table : tableMap.values()) {
            ((SingleSelectionModel) table.getSelectionModel()).clear();
        }
    }

    @Override
    public StringColumn getAsyncTypeIdColumn() {
        return asyncTypeIdColumn;
    }

    @Override
    public void initView() {
        enableButtons(false);
        initCommonTable();
        initFormTable();
        initEmailTable();
        initAsyncTable();
    }

    private void enableButtons(boolean isEnable){
        delLink.setEnabled(isEnable);
        checkButton.setEnabled(isEnable);
    }
}
