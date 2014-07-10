package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.GetConfigurationAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.GetConfigurationResult;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationResult;
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

public class ConfigurationPresenter	extends	Presenter<ConfigurationPresenter.MyView, ConfigurationPresenter.MyProxy> implements ConfigurationUiHandlers{
	
	public static final String TOKEN = "!configuration";

	@ProxyStandard
	@NameToken(TOKEN)
	public interface MyProxy extends ProxyPlace<ConfigurationPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<ConfigurationUiHandlers>{
        void setCommonConfigData(List<DataRow<Cell>> rowsData);
        void setFormConfigData(List<DataRow<Cell>> rowsData);
        RefBookColumn getDepartmentColumn();
        StringColumn getUploadPathColumn();
        StringColumn getArchivePathColumn();
        StringColumn getErrorPathColumn();
        RefBookColumn getParamColumn();
        StringColumn getValueColumn();
        List<DataRow<Cell>> getFormRowsData();
        List<DataRow<Cell>> getCommonRowsData();
	}

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;

	@Inject
	public ConfigurationPresenter(final EventBus eventBus, final MyView view,
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
		GetConfigurationAction action = new GetConfigurationAction();
		dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetConfigurationResult>() {
							@Override
							public void onSuccess(GetConfigurationResult result) {
								getView().setFormConfigData(getFormRowsData(result.getModel(), result.getDereferenceDepartmentNameMap()));
                                getView().setCommonConfigData(getCommonRowsData(result.getModel()));
							}

                            @Override
                            public void onFailure(Throwable caught) {
                                caught.printStackTrace();
                            }
                        	
                        }, this).addCallback(TaManualRevealCallback.create(this, placeManager)));
	}

    /**
     * Преобразование данных с серверной стороны в строки таблицы общих параметров
     */
    private List<DataRow<Cell>> getCommonRowsData(ConfigurationParamModel model) {
        List<DataRow<Cell>> rowsData = new LinkedList<DataRow<Cell>>();

        for (ConfigurationParam key : model.keySet()) {
            if (!key.isCommon()) {
                continue;
            }

            List<String> list = model.get(key, DepartmentType.ROOT_BANK.getCode());
            if (list == null) {
                continue;
            }

            for (String value : list) {
                DataRow<Cell> dataRow = createCommonDataRow();
                // Значения
                dataRow.getCell(getView().getParamColumn().getAlias()).setNumericValue(BigDecimal.valueOf(key.ordinal()));
                dataRow.getCell(getView().getParamColumn().getAlias()).setRefBookDereference(key.getCaption());
                dataRow.getCell(getView().getValueColumn().getAlias()).setStringValue(value);
                rowsData.add(dataRow);
            }
        }
        return rowsData;
    }

    /**
     * Преобразование данных с серверной стороны в строки таблицы загрузки НФ
     */
    private List<DataRow<Cell>> getFormRowsData(ConfigurationParamModel model, Map<Integer, String> departmentMap) {
        List<DataRow<Cell>> rowsData = new LinkedList<DataRow<Cell>>();

        Map<Integer, Map<ConfigurationParam, String>> rowMap = new HashMap<Integer, Map<ConfigurationParam, String>>();
        for (ConfigurationParam key : asList(ConfigurationParam.FORM_UPLOAD_DIRECTORY, ConfigurationParam.FORM_ARCHIVE_DIRECTORY, ConfigurationParam.FORM_ERROR_DIRECTORY)) {
            Map<Integer, List<String>> map = model.get(key);
            if (map != null) {
                for (int departmentId : map.keySet()) {
                    if (rowMap.get(departmentId) == null) {
                        rowMap.put(departmentId, new HashMap<ConfigurationParam, String>());
                    }
                    String str = null;
                    List<String> list = map.get(departmentId);
                    if (list != null && !list.isEmpty()) {
                        str = list.get(0);
                    }
                    rowMap.get(departmentId).put(key, str);
                }
            }
        }

        for (Map.Entry<Integer, Map<ConfigurationParam, String>> entry : rowMap.entrySet()) {
            DataRow<Cell> dataRow = createFormDataRow();
            rowsData.add(dataRow);
            // Значения
            dataRow.getCell(getView().getDepartmentColumn().getAlias()).setNumericValue(BigDecimal.valueOf(entry.getKey()));
            dataRow.getCell(getView().getDepartmentColumn().getAlias()).setRefBookDereference(departmentMap.get(entry.getKey()));
            dataRow.getCell(getView().getUploadPathColumn().getAlias()).setStringValue(entry.getValue().get(ConfigurationParam.FORM_UPLOAD_DIRECTORY));
            dataRow.getCell(getView().getArchivePathColumn().getAlias()).setStringValue(entry.getValue().get(ConfigurationParam.FORM_ARCHIVE_DIRECTORY));
            dataRow.getCell(getView().getErrorPathColumn().getAlias()).setStringValue(entry.getValue().get(ConfigurationParam.FORM_ERROR_DIRECTORY));
        }
        return rowsData;
    }

	@Override
	public boolean useManualReveal() {
		return true;
	}

    /**
     * Обработка строки. Пустая строка считается как null
     */
    private String cleanString(String str) {
        if (str == null) {
            return null;
        }
        str = str.trim();
        if (str.isEmpty()) {
            return null;
        }
        return str;
    }

	@Override
	public void onSave() {
        LogCleanEvent.fire(this);
        ConfigurationParamModel model = new ConfigurationParamModel();
        SaveConfigurationAction action = new SaveConfigurationAction();
        action.setModel(model);
        // Преобразование данных таблиц в ConfigurationParamModel
        List<DataRow<Cell>> commonRowsData = getView().getCommonRowsData();
        List<DataRow<Cell>> formRowsData = getView().getFormRowsData();

        // Общие параметры
        Map<ConfigurationParam, List<String>> commonMap = new HashMap<ConfigurationParam, List<String>>();

        // Группировка по параметру
        for (DataRow<Cell> dataRow : commonRowsData) {
            BigDecimal paramId = dataRow.getCell(getView().getParamColumn().getAlias()).getNumericValue();
            ConfigurationParam param = null;
            if (paramId != null) {
                param = ConfigurationParam.values()[paramId.intValue()];
            }
            String value = cleanString(dataRow.getCell(getView().getValueColumn().getAlias()).getStringValue());
            if (param == null || value == null) {
                // Не полностью заполненные параметры не сохраняем
                continue;
            }
            if (!commonMap.containsKey(param)) {
                commonMap.put(param, new LinkedList<String>());
            }
            commonMap.get(param).add(value);
        }
        for (Map.Entry<ConfigurationParam, List<String>> entry : commonMap.entrySet()) {
            Map<Integer, List<String>> departmentMap = new HashMap<Integer, List<String>>();
            departmentMap.put(DepartmentType.ROOT_BANK.getCode(), entry.getValue());
            model.put(entry.getKey(), departmentMap);
        }

        // Параметры загрузки НФ
        Set<Integer> departmentSet = new HashSet<Integer>();
        for (DataRow<Cell> dataRow : formRowsData) {
            BigDecimal departmentId = dataRow.getCell(getView().getDepartmentColumn().getAlias()).getNumericValue();
            String uploadPath = cleanString(dataRow.getCell(getView().getUploadPathColumn().getAlias()).getStringValue());
            String archivePath = cleanString(dataRow.getCell(getView().getArchivePathColumn().getAlias()).getStringValue());
            String errorPath = cleanString(dataRow.getCell(getView().getErrorPathColumn().getAlias()).getStringValue());

            if (departmentId == null || (uploadPath == null && archivePath == null && errorPath == null)) {
                continue;
            }
            if (uploadPath != null) {
                model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId.intValue(), asList(uploadPath));
            }
            if (archivePath != null) {
                model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, departmentId.intValue(), asList(archivePath));
            }
            if (errorPath != null) {
                model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, departmentId.intValue(), asList(errorPath));
            }
            // Проверка дублей
            if (!departmentSet.add(departmentId.intValue())) {
                action.getDublicateDepartmentIdSet().add(departmentId.intValue());
            }
        }

        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SaveConfigurationResult>() {
            @Override
            public void onSuccess(SaveConfigurationResult result) {
                placeManager.revealCurrentPlace();
            }
        }, this));
	}

	@Override
	public void onCancel() {
		placeManager.revealCurrentPlace();
	}

    /**
     * Подготовка строки таблицы общих параметров
     */
    private DataRow createCommonDataRow() {
        DataRow<Cell> dataRow = new DataRow<Cell>();

        Cell paramCell = new Cell();
        paramCell.setColumn(getView().getParamColumn());
        paramCell.setEditable(true);

        Cell valueCell = new Cell();
        valueCell.setColumn(getView().getValueColumn());
        valueCell.setEditable(true);

        dataRow.setFormColumns(asList(paramCell, valueCell));
        return dataRow;
    }

    /**
     * Подготовка строки таблицы параметров загрузки НФ
     */
    private DataRow createFormDataRow() {
        DataRow<Cell> dataRow = new DataRow<Cell>();

        Cell departmentCell = new Cell();
        departmentCell.setColumn(getView().getDepartmentColumn());
        departmentCell.setEditable(true);

        Cell uploadCell = new Cell();
        uploadCell.setColumn(getView().getUploadPathColumn());
        uploadCell.setEditable(true);

        Cell archiveCell = new Cell();
        archiveCell.setColumn(getView().getArchivePathColumn());
        archiveCell.setEditable(true);

        Cell errorCell = new Cell();
        errorCell.setColumn(getView().getErrorPathColumn());
        errorCell.setEditable(true);

        dataRow.setFormColumns(asList(departmentCell, uploadCell, archiveCell, errorCell));
        return dataRow;
    }

    @Override
    public void onFormAddRow() {
        List<DataRow<Cell>> data = getView().getFormRowsData();
        data.add(createFormDataRow());
        getView().setFormConfigData(data);
    }

    @Override
    public void onCommonAddRow() {
        List<DataRow<Cell>> data = getView().getCommonRowsData();
        data.add(createCommonDataRow());
        getView().setCommonConfigData(data);
    }
}
