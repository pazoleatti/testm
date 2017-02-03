package com.aplana.sbrf.taxaccounting.web.module.commonparameter.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.GetCommonParameterAction;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.GetCommonParameterResult;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.SaveCommonParameterAction;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.SaveCommonParameterResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;

public class CommonParameterPresenter extends Presenter<CommonParameterPresenter.MyView, CommonParameterPresenter.MyProxy> implements CommonParameterUiHandlers {
    public static final String TOKEN = "!commonParameter";

    private ConfigurationParamModel initData;
    private List<DataRow<Cell>> rowsData = new ArrayList<DataRow<Cell>>();

    private Map<ConfigurationParam, String> defaultParamValues = new HashMap<ConfigurationParam, String>() {{
        put(ConfigurationParam.SBERBANK_INN, "7707083893");
        put(ConfigurationParam.NO_CODE, "9979");
    }};

    private static final String CANCEL_DIALOG_TITLE = "Отмена операции";
    private static final String CANCEL_DIALOG_TEXT = "На форме общих параметров системы имеются несохраненные данные. Выйти без сохранения?";

    private static final String RESTORE_DIALOG_TITLE = "Восстановление значений по умолчанию";
    private static final String RESTORE_DIALOG_TEXT = "Вы уверены, что хотите вернуть значения параметров по умолчанию? Все текущие значения будут заменены.";

    @ProxyStandard
    @NameToken(TOKEN)
    public interface MyProxy extends ProxyPlace<CommonParameterPresenter> {
    }

    public interface MyView extends View, HasUiHandlers<CommonParameterUiHandlers> {
        RefBookColumn getParamColumn();

        StringColumn getValueColumn();

        DataRow<Cell> getSelectedObject();

        void initView();

        void setConfigData(List<DataRow<Cell>> rowsData);

        void refreshGrid(List<DataRow<Cell>> rowsData);
    }

    private final DispatchAsync dispatcher;
    private final TaPlaceManager placeManager;

    @Inject
    public CommonParameterPresenter(final EventBus eventBus, final MyView view,
                                    final MyProxy proxy, PlaceManager placeManager,
                                    DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        getView().setUiHandlers(this);
        this.placeManager = (TaPlaceManager) placeManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        rowsData.clear();
        getView().initView();
    }

    /**
     * Генерация данных для грида
     */
    private List<DataRow<Cell>> createGridData(ConfigurationParamModel model) {
        List<DataRow<Cell>> rowsData = new LinkedList<DataRow<Cell>>();

        // Создаем строки
        for (ConfigurationParam confParam : ConfigurationParam.values()) {
            if (!confParam.getGroup().equals(ConfigurationParamGroup.COMMON_PARAM)) {
                continue;
            }

            DataRow<Cell> dataRow = createDataRow();
            dataRow.getCell(getView().getParamColumn().getAlias()).setNumericValue(BigDecimal.valueOf(confParam.ordinal()));
            dataRow.getCell(getView().getParamColumn().getAlias()).setRefBookDereference(confParam.getCaption());
            dataRow.getCell(getView().getValueColumn().getAlias()).setStringValue(model.getFullStringValue(confParam, 0));
            rowsData.add(dataRow);
        }

        Collections.sort(rowsData, PARAM_COLUMN_COMPARATOR);

        return rowsData;
    }

    /**
     * Проверка изменились ли данные или нет
     */
    private boolean isDataChanged() {
        List<DataRow<Cell>> initGridData = createGridData(initData);

        for (int i = 0; i < initGridData.size(); i++) {
            String initValue = initGridData.get(i).getCell(getView().getValueColumn().getAlias()).getStringValue();
            String actualValue = rowsData.get(i).getCell(getView().getValueColumn().getAlias()).getStringValue();

            if (!StringUtils.equals(initValue, actualValue)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void onSaveClicked() {
        if (isDataChanged()) {
            saveChanges();
        }
    }

    @Override
    public void onCancel() {
        if (isDataChanged()) {
            Dialog.confirmMessageYesClose(CANCEL_DIALOG_TITLE, CANCEL_DIALOG_TEXT, new DialogHandler() {
                @Override
                public void yes() {
                    cancelChanges();
                    Dialog.hideMessage();
                }
            });
        }
    }

    private void cancelChanges() {
        loadData();
        getView().setConfigData(getRowsData());
    }

    @Override
    public void onRestore() {
        Dialog.confirmMessageYesClose(RESTORE_DIALOG_TITLE, RESTORE_DIALOG_TEXT, new DialogHandler() {
            @Override
            public void yes() {
                restoreChanges();
                Dialog.hideMessage();
            }
        });
    }

    private void restoreChanges() {
        List<DataRow<Cell>> gridRows = getRowsData();

        for (DataRow<Cell> gridRow : gridRows) {
            Cell cellParam = gridRow.getCell(getView().getParamColumn().getAlias());
            Cell cellValue = gridRow.getCell(getView().getValueColumn().getAlias());

            ConfigurationParam configurationParam = ConfigurationParam.values()[cellParam.getNumericValue().intValue()];
            cellValue.setStringValue(defaultParamValues.get(configurationParam));
        }

        getView().refreshGrid(gridRows);
        saveChanges();
    }

    private void saveChanges() {
        List<DataRow<Cell>> gridRows = getRowsData();
        Map<ConfigurationParam, String> configurationParamMap = new HashMap<ConfigurationParam, String>();

        for (DataRow<Cell> gridRow : gridRows) {
            Cell cellParam = gridRow.getCell(getView().getParamColumn().getAlias());
            Cell cellValue = gridRow.getCell(getView().getValueColumn().getAlias());

            ConfigurationParam configurationParam = ConfigurationParam.values()[cellParam.getNumericValue().intValue()];

            configurationParamMap.put(configurationParam, cellValue.getStringValue());
        }

        SaveCommonParameterAction action = new SaveCommonParameterAction();
        action.setConfigurationParamMap(configurationParamMap);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SaveCommonParameterResult>() {
            @Override
            public void onSuccess(SaveCommonParameterResult result) {
                LogCleanEvent.fire(CommonParameterPresenter.this);

                if (result.getUuid() != null) {
                    LogAddEvent.fire(CommonParameterPresenter.this, result.getUuid());
                    LogShowEvent.fire(CommonParameterPresenter.this, true);
                }
            }
        }, this));
    }

    /**
     * Подготовка строки таблицы
     */
    private DataRow<Cell> createDataRow() {
        DataRow<Cell> dataRow = new DataRow<Cell>();

        Cell paramCell = new Cell();
        paramCell.setColumn(getView().getParamColumn());
        paramCell.setEditable(false);

        Cell valueCell = new Cell();
        valueCell.setColumn(getView().getValueColumn());
        valueCell.setEditable(true);

        dataRow.setFormColumns(asList(paramCell, valueCell));

        return dataRow;
    }

    private void loadData() {
        dispatcher.execute(new GetCommonParameterAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetCommonParameterResult>() {
                            @Override
                            public void onSuccess(GetCommonParameterResult result) {
                                LogCleanEvent.fire(CommonParameterPresenter.this);

                                initData = result.getModel();
                                rowsData = createGridData(result.getModel());
                                getView().setConfigData(rowsData);
                            }
                        }, this).addCallback(TaManualRevealCallback.create(this, placeManager)));
    }

    @Override
    public List<DataRow<Cell>> getRowsData() {
        if (!rowsData.isEmpty()) {
            return rowsData;
        } else {
            loadData();
            return new ArrayList<DataRow<Cell>>(0);
        }
    }

    private static final Comparator<DataRow<Cell>> PARAM_COLUMN_COMPARATOR = new Comparator<DataRow<Cell>>() {
        @Override
        public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
            return o1.getCell("paramColumn").getRefBookDereference().compareTo(o2.getCell("paramColumn").getRefBookDereference());
        }
    };
}
