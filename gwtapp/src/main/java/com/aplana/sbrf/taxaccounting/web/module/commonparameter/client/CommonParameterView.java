package com.aplana.sbrf.taxaccounting.web.module.commonparameter.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
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
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

public class CommonParameterView extends ViewWithUiHandlers<CommonParameterUiHandlers> implements CommonParameterPresenter.MyView {

    interface Binder extends UiBinder<Widget, CommonParameterView> {
    }

    private final SingleSelectionModel<DataRow<Cell>> singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();

    @UiField
    GenericDataGrid<DataRow<Cell>> commonTable;

    @UiField
    Label titleLabel;

    @UiField
    Widget commonPanel;

    @UiField
    Button restoreButton;

    private RefBookColumn paramColumn = new RefBookColumn();
    private StringColumn valueColumn = new StringColumn();

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    @Inject
    public CommonParameterView(final Binder binder) {
        initWidget(binder.createAndBindUi(this));
        commonTable.setSelectionModel(singleSelectionModel);
        commonTable.setMinimumTableWidth(20, Style.Unit.EM);
        commonTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
        initCommonTable();
    }

    /**
     * Подготовка таблицы общих параметров
     */
    private void initCommonTable() {
        paramColumn.setId(1);
        paramColumn.setRefBookAttributeId(1041L);
        paramColumn.setRefBookAttribute(new RefBookAttribute(){{setAttributeType(RefBookAttributeType.STRING);}});
        paramColumn.setFilter(getConfParamFilter(ConfigurationParamGroup.COMMON_PARAM));
        paramColumn.setAlias("paramColumn");
        paramColumn.setName("Параметр");
        paramColumn.setSearchEnabled(false);

        valueColumn.setAlias("valueColumn");
        valueColumn.setName("Значение параметра");
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



    @UiHandler("saveButton")
    void onSaveButtonClick(ClickEvent event) {
        getUiHandlers().onSaveClicked();
    }

    @UiHandler("cancelButton")
    void onCancelButtonClick(ClickEvent event) {
        getUiHandlers().onCancel();
    }

    @UiHandler("restoreButton")
    void onCheckButtonClick(ClickEvent event) {
        getUiHandlers().onRestore();
    }

    @Override
    public void setConfigData(List<DataRow<Cell>> rowsData) {
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
    public void refreshGrid(List<DataRow<Cell>> rowsData) {
        commonTable.redraw();
    }

    @Override
    public void initView() {
        Column<DataRow<Cell>, ?> paramColumnUI = factory.createTableColumn(paramColumn, commonTable);
        Column<DataRow<Cell>, ?> paramValueUI = factory.createTableColumn(valueColumn, commonTable);
        commonTable.removeAllColumns();
        commonTable.setColumnWidth(paramColumnUI, 30, Style.Unit.EM);
        commonTable.addColumn(paramColumnUI, paramColumn.getName());
        commonTable.addColumn(paramValueUI, valueColumn.getName());

        // Обновляем таблицу после обновления модели
        ((DataRowColumn<?>) paramValueUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    commonTable.redraw();
                }
            }
        });

        setConfigData(getUiHandlers().getRowsData());
    }

    @Override
    public DataRow<Cell> getSelectedObject() {
        return singleSelectionModel.getSelectedObject();
    }

    private void updateStyle(int rowIndex, boolean valid) {
        if (valid) {
            commonTable.getRowElement(rowIndex).getCells().getItem(1).removeClassName("alert-cell");
        } else {
            commonTable.getRowElement(rowIndex).getCells().getItem(1).addClassName("alert-cell");
        }
    }

    @Override
    public void updateStyle(ConfigurationParam configurationParam, boolean valid) {
        updateStyle(ConfigurationParam.SBERBANK_INN.equals(configurationParam) ? 0 : 1, valid);
    }
}
