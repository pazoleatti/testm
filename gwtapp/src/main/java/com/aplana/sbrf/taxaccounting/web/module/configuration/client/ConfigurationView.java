package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
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
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers> implements MyView {
    @UiField
    GenericDataGrid<DataRow<Cell>> commonTable;

    @UiField
    Label titleLabel;

    @UiField
    LinkButton commonLink, formLink, emailLink, asyncLink;

    @UiField
    Widget commonPanel;

    @UiField
    LinkButton addLink;

    @UiField
    LinkButton delLink;

    @UiField
    Button checkButton;

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

    private final SingleSelectionModel<DataRow<Cell>> singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    interface Binder extends UiBinder<Widget, ConfigurationView> {
    }

    @Inject
    public ConfigurationView(final Binder binder) {
        initWidget(binder.createAndBindUi(this));
        commonTable.setSelectionModel(singleSelectionModel);
        commonTable.setMinimumTableWidth(20, Style.Unit.EM);
        commonTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
        initCommonTable();
        initFormTable();
        initAsyncTable();
        initEmailTable();
        singleSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (commonPanel.isVisible()){
                    enableButtons(singleSelectionModel.getSelectedObject() != null);
                }
            }
        });
    }

    /**
     * Подготовка таблицы общих параметров
     */
    private void initCommonTable() {
        paramColumn.setId(1);
        paramColumn.setRefBookAttributeId1(1041L);
        paramColumn.setRefBookAttribute(new RefBookAttribute(){{setAttributeType(RefBookAttributeType.STRING);}});
        // Допустимые значения
        paramColumn.setFilter(getConfParamFilter(ConfigurationParamGroup.COMMON));
        paramColumn.setAlias("paramColumn");
        paramColumn.setName("Параметр");
        paramColumn.setSearchEnabled(false);

        valueColumn.setAlias("valueColumn");
        valueColumn.setName("Значение параметра");
    }

    /**
     * Подготовка таблицы параметров загрузки НФ
     */
    private void initFormTable() {

        departmentColumn.setId(1);
        departmentColumn.setNameAttributeId(161L);
        departmentColumn.setRefBookAttributeId1(161L);
        departmentColumn.setRefBookAttribute(new RefBookAttribute(){{setAttributeType(RefBookAttributeType.STRING);}});
        departmentColumn.setHierarchical(true);
        departmentColumn.setFilter("TYPE=2"); // ТБ
        departmentColumn.setAlias("departmentColumn");
        departmentColumn.setName("Наименование ТБ");
        departmentColumn.setWidth(1);

        uploadPathColumn.setAlias("uploadPathColumn");
        uploadPathColumn.setName("Путь к каталогу загрузки");

        archivePathColumn.setAlias("archivePathColumn");
        archivePathColumn.setName("Путь к каталогу aрхива");

        errorPathColumn.setAlias("errorPathColumn");
        errorPathColumn.setName("Путь к каталогу ошибок");
    }

    /**
     * Подготовка таблицы параметров электронной почты
     */
    private void initEmailTable() {
        emailNameColumn.setAlias("emailNameColumn");
        emailNameColumn.setName("Параметр");

        emailValueColumn.setAlias("emailValueColumn");
        emailValueColumn.setName("Значение параметра");

        emailDescriptionColumn.setAlias("emailCodeColumn");
        emailDescriptionColumn.setName("Описание");
    }

    /**
     * Подготовка таблицы параметров электронной почты
     */
    private void initAsyncTable() {
        asyncTypeIdColumn.setAlias("asyncTypeIdColumn");
        asyncTypeIdColumn.setName("");

        asyncTypeColumn.setAlias("asyncTypeColumn");
        asyncTypeColumn.setName("Тип задания");

        asyncLimitKindColumn.setAlias("asyncLimitKindColumn");
        asyncLimitKindColumn.setName("Вид ограничения");

        asyncLimitColumn.setAlias("asyncLimitColumn");
        asyncLimitColumn.setName("Значение параметра \"Ограничение на выполнение задания\"");

        asyncShortLimitColumn.setAlias("asyncShortLimitColumn");
        asyncShortLimitColumn.setName("Значение параметра \"Ограничение на выполнение задания в очереди быстрых заданий\"");
        /*final SingleSelectionModel<DataRow<Cell>> singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();
        asyncTable.setSelectionModel(singleSelectionModel);
        singleSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (asyncPanel.isVisible()) {
                    enableButtons(singleSelectionModel.getSelectedObject() != null);
                }
            }
        });*/
    }

    private void changeToCommon(){
        while (commonTable.getColumnCount() > 0) {
            commonTable.removeAllColumns();
        }
        Column<DataRow<Cell>, ?> paramColumnUI = factory.createTableColumn(paramColumn, commonTable);
        commonTable.setColumnWidth(paramColumnUI, 30, Style.Unit.EM);
        commonTable.addColumn(paramColumnUI, paramColumn.getName());
        commonTable.addColumn(factory.createTableColumn(valueColumn, commonTable), valueColumn.getName());

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

    private void changeToForm(){
        while (commonTable.getColumnCount() > 0) {
            commonTable.removeColumn(0);
        }
        Column<DataRow<Cell>, ?> departmentColumnUI = factory.createTableColumn(departmentColumn, commonTable);
        commonTable.setColumnWidth(departmentColumnUI, 20, Style.Unit.EM);
        commonTable.addColumn(departmentColumnUI, departmentColumn.getName());
        commonTable.addColumn(factory.createTableColumn(uploadPathColumn, commonTable), uploadPathColumn.getName());
        commonTable.addColumn(factory.createTableColumn(archivePathColumn, commonTable), archivePathColumn.getName());
        commonTable.addColumn(factory.createTableColumn(errorPathColumn, commonTable), errorPathColumn.getName());

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>) departmentColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    commonTable.redraw();
                }
            }
        });
    }

    private void changeToEmail(){
        while (commonTable.getColumnCount() > 0) {
            commonTable.removeColumn(0);
        }
        Column<DataRow<Cell>, ?> emailParamColumnUI = factory.createTableColumn(emailNameColumn, commonTable);
        commonTable.setColumnWidth(emailParamColumnUI, 20, Style.Unit.EM);
        commonTable.addColumn(emailParamColumnUI, emailNameColumn.getName());

        emailParamColumnUI = factory.createTableColumn(emailValueColumn, commonTable);
        commonTable.setColumnWidth(emailParamColumnUI, 30, Style.Unit.EM);
        commonTable.addColumn(emailParamColumnUI, emailValueColumn.getName());
        commonTable.addColumn(factory.createTableColumn(emailDescriptionColumn, commonTable), emailDescriptionColumn.getName());

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>) emailParamColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    commonTable.redraw();
                }
            }
        });
    }

    private void changeToAsync(){
        while (commonTable.getColumnCount() > 0) {
            commonTable.removeColumn(0);
        }
        Column<DataRow<Cell>, ?> asyncParamColumnUI = factory.createTableColumn(asyncTypeIdColumn, commonTable);

        asyncParamColumnUI = factory.createTableColumn(asyncTypeColumn, commonTable);
        commonTable.setColumnWidth(asyncParamColumnUI, 20, Style.Unit.EM);
        commonTable.addColumn(asyncParamColumnUI, asyncTypeColumn.getName());

        asyncParamColumnUI = factory.createTableColumn(asyncLimitKindColumn, commonTable);
        commonTable.setColumnWidth(asyncParamColumnUI, 20, Style.Unit.EM);
        commonTable.addColumn(asyncParamColumnUI, asyncLimitKindColumn.getName());

        asyncParamColumnUI = factory.createTableColumn(asyncLimitColumn, commonTable);
        commonTable.setColumnWidth(asyncParamColumnUI, 20, Style.Unit.EM);
        commonTable.addColumn(asyncParamColumnUI, asyncLimitColumn.getName());

        asyncParamColumnUI = factory.createTableColumn(asyncShortLimitColumn, commonTable);
        commonTable.setColumnWidth(asyncParamColumnUI, 20, Style.Unit.EM);
        commonTable.addColumn(asyncParamColumnUI, asyncShortLimitColumn.getName());

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>) asyncParamColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    commonTable.redraw();
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
        titleLabel.setText(group.getCaption());

        commonLink.setVisible(!group.equals(ConfigurationParamGroup.COMMON));

        formLink.setVisible(!group.equals(ConfigurationParamGroup.FORM));

        emailLink.setVisible(!group.equals(ConfigurationParamGroup.EMAIL));

        asyncLink.setVisible(!group.equals(ConfigurationParamGroup.ASYNC));

        addLink.setVisible(!group.equals(ConfigurationParamGroup.EMAIL) && !group.equals(ConfigurationParamGroup.ASYNC));
        delLink.setVisible(!group.equals(ConfigurationParamGroup.EMAIL) && !group.equals(ConfigurationParamGroup.ASYNC));

       /* for (DataGrid<DataRow<Cell>> table : tableMap.values()) {
            table.redraw();
        }*/
    }

    @UiHandler("addLink")
    void onAddLinkClick(ClickEvent event) {
        getUiHandlers().onAddRow();
    }



    @UiHandler("delLink")
    void onDelLinkClick(ClickEvent event) {
        DataRow<Cell> selRow = singleSelectionModel.getSelectedObject();
        if (selRow == null) {
            return;
        }
        getUiHandlers().onDeleteItem();
    }

    @UiHandler("commonLink")
    void onCommonLinkClick(ClickEvent event) {
        singleSelectionModel.clear();
        changeToCommon();
        showTab(ConfigurationParamGroup.COMMON);
        setConfigData(getUiHandlers().getRowsData(ConfigurationParamGroup.COMMON));
        enableButtons(((SingleSelectionModel)commonTable.getSelectionModel()).getSelectedObject() != null);
    }

    @UiHandler("formLink")
    void onFormLinkClick(ClickEvent event) {
        singleSelectionModel.clear();
        changeToForm();
        showTab(ConfigurationParamGroup.FORM);
        setConfigData(getUiHandlers().getRowsData(ConfigurationParamGroup.FORM));
        enableButtons(((SingleSelectionModel) commonTable.getSelectionModel()).getSelectedObject() != null);
    }

    @UiHandler("emailLink")
    void onEmailLinkClick(ClickEvent event) {
        singleSelectionModel.clear();
        changeToEmail();
        showTab(ConfigurationParamGroup.EMAIL);
        setConfigData(getUiHandlers().getRowsData(ConfigurationParamGroup.EMAIL));
        enableButtons(((SingleSelectionModel) commonTable.getSelectionModel()).getSelectedObject() != null);
    }

    @UiHandler("asyncLink")
    void onAsyncLinkClick(ClickEvent event) {
        singleSelectionModel.clear();
        changeToAsync();
        showTab(ConfigurationParamGroup.ASYNC);
        setConfigData(getUiHandlers().getRowsData(ConfigurationParamGroup.ASYNC));
        enableButtons(((SingleSelectionModel) commonTable.getSelectionModel()).getSelectedObject() != null);
    }

    @UiHandler("checkButton")
    void onCheckButtonClick(ClickEvent event) {
//        DataGrid<DataRow<Cell>> table = getTable(activeGroup);
        getUiHandlers().onCheckAccess(false);
    }

    @UiHandler("saveButton")
    void onSaveButtonClick(ClickEvent event) {
        getUiHandlers().onSaveClicked();
    }

    @UiHandler("cancelButton")
    void onCancelButtonClick(ClickEvent event) {
        getUiHandlers().onCancel();
    }


    @Override
    public void setConfigData(List<DataRow<Cell>> rowsData) {
        /*if (needSort) {
            Collections.sort(rowsData, comparatorMap.get(group));
        }*/

        commonTable.setVisibleRange(new Range(0, rowsData.size()));
        commonTable.setRowCount(rowsData.size());
        commonTable.setRowData(0, rowsData);
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
        singleSelectionModel.clear();
    }

    @Override
    public StringColumn getAsyncTypeIdColumn() {
        return asyncTypeIdColumn;
    }

    @Override
    public void initView() {
        //enableButtons(false);
        changeToCommon();
        setConfigData(getUiHandlers().getRowsData(ConfigurationParamGroup.COMMON));
    }

    @Override
    public DataRow<Cell> getSelectedObject() {
        return singleSelectionModel.getSelectedObject();
    }

    private void enableButtons(boolean isEnable){
        delLink.setEnabled(isEnable);
        checkButton.setEnabled(isEnable);
    }
}
