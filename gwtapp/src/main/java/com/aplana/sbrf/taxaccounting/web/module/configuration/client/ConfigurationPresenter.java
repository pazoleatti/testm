package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.*;
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

public class ConfigurationPresenter extends Presenter<ConfigurationPresenter.MyView, ConfigurationPresenter.MyProxy>
        implements ConfigurationUiHandlers {

    public static final String TOKEN = "!configuration";

    private final Map<ConfigurationParamGroup, List<DataRow<Cell>>> rowsDataMap = new HashMap<ConfigurationParamGroup, List<DataRow<Cell>>>();
    private final Map<ConfigurationParamGroup, Comparator<DataRow<Cell>>> comparatorMap = new HashMap<ConfigurationParamGroup, Comparator<DataRow<Cell>>>();
    private ConfigurationParamGroup activeGroup = ConfigurationParamGroup.COMMON;

    @ProxyStandard
    @NameToken(TOKEN)
    public interface MyProxy extends ProxyPlace<ConfigurationPresenter> {
    }

    public interface MyView extends View, HasUiHandlers<ConfigurationUiHandlers> {
        void setConfigData(List<DataRow<Cell>> rowsData);

        RefBookColumn getParamColumn();

        StringColumn getValueColumn();

        RefBookColumn getDepartmentColumn();

        StringColumn getUploadPathColumn();

        StringColumn getArchivePathColumn();

        StringColumn getErrorPathColumn();

        StringColumn getEmailNameColumn();

        StringColumn getEmailValueColumn();

        StringColumn getEmailDescriptionColumn();

        StringColumn getAsyncTypeColumn();

        StringColumn getAsyncLimitKindColumn();

        StringColumn getAsyncLimitColumn();

        StringColumn getAsyncShortLimitColumn();

        void clearSelection();

        StringColumn getAsyncTypeIdColumn();

        void initView();

        DataRow<Cell> getSelectedObject();
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
        rowsDataMap.clear();
        getView().initView();
        initMaps();
        /*GetConfigurationAction action = new GetConfigurationAction();
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetConfigurationResult>() {
                            @Override
                            public void onSuccess(GetConfigurationResult result) {
                                getView().setConfigData(ConfigurationParamGroup.COMMON, getCommonRowsData(result.getModel()), true);
                                *//*getView().setConfigData(ConfigurationParamGroup.FORM, getFormRowsData(result.getModel(),
                                        result.getDereferenceDepartmentNameMap()), true);
                                getView().setConfigData(ConfigurationParamGroup.EMAIL, getEmailRowsData(result.getEmailConfigs()), false);
                                getView().setConfigData(ConfigurationParamGroup.ASYNC, getAsyncRowsData(result.getAsyncConfigs()), true);*//*
                            }
                        }, this).addCallback(TaManualRevealCallback.create(this, placeManager)));*/
    }


    /**
     * Преобразование данных с серверной стороны в строки таблицы общих параметров
     */
    private List<DataRow<Cell>> getCommonRowsData(ConfigurationParamModel model) {
        List<DataRow<Cell>> rowsData = new LinkedList<DataRow<Cell>>();

        for (ConfigurationParam key : model.keySet()) {
            if (!key.getGroup().equals(ConfigurationParamGroup.COMMON)) {
                continue;
            }

            String value = model.getFullStringValue(key, 0);
            if (value == null || value.isEmpty()) {
                continue;
            }

            DataRow<Cell> dataRow = createDataRow(ConfigurationParamGroup.COMMON);
            // Значения
            dataRow.getCell(getView().getParamColumn().getAlias()).setNumericValue(BigDecimal.valueOf(key.ordinal()));
            dataRow.getCell(getView().getParamColumn().getAlias()).setRefBookDereference(key.getCaption());
            dataRow.getCell(getView().getValueColumn().getAlias()).setStringValue(value);
            rowsData.add(dataRow);
        }
        return rowsData;
    }

    /**
     * Преобразование данных с серверной стороны в строки таблицы загрузки НФ
     */
    private List<DataRow<Cell>> getFormRowsData(ConfigurationParamModel model, Map<Integer, String> departmentMap) {
        List<DataRow<Cell>> rowsData = new LinkedList<DataRow<Cell>>();

        Map<Integer, Map<ConfigurationParam, String>> rowMap = new HashMap<Integer, Map<ConfigurationParam, String>>();
        for (ConfigurationParam key : asList(ConfigurationParam.FORM_UPLOAD_DIRECTORY,
                ConfigurationParam.FORM_ARCHIVE_DIRECTORY, ConfigurationParam.FORM_ERROR_DIRECTORY)) {
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
            DataRow<Cell> dataRow = createDataRow(ConfigurationParamGroup.FORM);
            rowsData.add(dataRow);
            // Значения
            dataRow.getCell(getView().getDepartmentColumn().getAlias())
                    .setNumericValue(BigDecimal.valueOf(entry.getKey()));
            dataRow.getCell(getView().getDepartmentColumn().getAlias())
                    .setRefBookDereference(departmentMap.get(entry.getKey()));
            dataRow.getCell(getView().getUploadPathColumn().getAlias())
                    .setStringValue(entry.getValue().get(ConfigurationParam.FORM_UPLOAD_DIRECTORY));
            dataRow.getCell(getView().getArchivePathColumn().getAlias())
                    .setStringValue(entry.getValue().get(ConfigurationParam.FORM_ARCHIVE_DIRECTORY));
            dataRow.getCell(getView().getErrorPathColumn().getAlias())
                    .setStringValue(entry.getValue().get(ConfigurationParam.FORM_ERROR_DIRECTORY));
        }
        return rowsData;
    }

    /**
     * Преобразование данных с серверной стороны в строки таблицы параметров электронной почты
     * @param params
     */
    private List<DataRow<Cell>> getEmailRowsData(List<Map<String, String>> params) {
        List<DataRow<Cell>> rowsData = new LinkedList<DataRow<Cell>>();

        for (Map<String, String> record : params) {
            DataRow<Cell> dataRow = createDataRow(ConfigurationParamGroup.EMAIL);
            // Значения
            dataRow.getCell(getView().getEmailNameColumn().getAlias()).setStringValue(record.get(ConfigurationParamModel.EMAIL_NAME_ATTRIBUTE));
            dataRow.getCell(getView().getEmailValueColumn().getAlias()).setStringValue(record.get(ConfigurationParamModel.EMAIL_VALUE_ATTRIBUTE));
            dataRow.getCell(getView().getEmailDescriptionColumn().getAlias()).setStringValue(record.get(ConfigurationParamModel.EMAIL_DESCRIPTION_ATTRIBUTE));
            rowsData.add(dataRow);
        }
        return rowsData;
    }

    /**
     * Преобразование данных с серверной стороны в строки таблицы параметров электронной почты
     * @param params
     */
    private List<DataRow<Cell>> getAsyncRowsData(List<Map<String, String>> params) {
        List<DataRow<Cell>> rowsData = new LinkedList<DataRow<Cell>>();

        for (Map<String, String> record : params) {
            DataRow<Cell> dataRow = createDataRow(ConfigurationParamGroup.ASYNC);
            // Значения
            dataRow.getCell(getView().getAsyncTypeIdColumn().getAlias()).setStringValue(record.get(ConfigurationParamModel.ASYNC_TYPE_ID));
            dataRow.getCell(getView().getAsyncTypeColumn().getAlias()).setStringValue(record.get(ConfigurationParamModel.ASYNC_TYPE));
            dataRow.getCell(getView().getAsyncLimitKindColumn().getAlias()).setStringValue(record.get(ConfigurationParamModel.ASYNC_LIMIT_KIND));
            dataRow.getCell(getView().getAsyncLimitColumn().getAlias()).setStringValue(record.get(ConfigurationParamModel.ASYNC_LIMIT));
            dataRow.getCell(getView().getAsyncShortLimitColumn().getAlias()).setStringValue(record.get(ConfigurationParamModel.ASYNC_SHORT_LIMIT));
            rowsData.add(dataRow);
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

    /**
     * Заполнение модели конфигурационных параметров
     */
    private void fillModel(ConfigurationParamModel model, List<DataRow<Cell>> commonRowsData,
                           List<DataRow<Cell>> formRowsData,
                           Set<Integer> duplicateDepartmentIdSet, Map<Integer, Set<String>> notSetFields) {
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
            for (String value: entry.getValue()) {
                model.setFullStringValue(entry.getKey(), 0, value);
            }
        }

        // Параметры загрузки НФ
        Set<Integer> departmentSet = new HashSet<Integer>();
        for (DataRow<Cell> dataRow : formRowsData) {
            BigDecimal departmentId = dataRow.getCell(getView().getDepartmentColumn().getAlias()).getNumericValue();
            String uploadPath = cleanString(dataRow.getCell(getView().getUploadPathColumn().getAlias()).getStringValue());
            String archivePath = cleanString(dataRow.getCell(getView().getArchivePathColumn().getAlias()).getStringValue());
            String errorPath = cleanString(dataRow.getCell(getView().getErrorPathColumn().getAlias()).getStringValue());

            if (notSetFields != null && (departmentId == null || uploadPath == null || archivePath == null || errorPath == null)) {
                Integer idInt = departmentId == null ? null : departmentId.intValue();
                if (notSetFields.get(idInt) == null) {
                    notSetFields.put(idInt, new HashSet<String>());
                }
                Set<String> notSetFieldSet = notSetFields.get(idInt);
                if (departmentId == null) {
                    notSetFieldSet.add("Подразделение ТБ");
                } else {
                    if (uploadPath == null) {
                        notSetFieldSet.add(ConfigurationParam.FORM_UPLOAD_DIRECTORY.getCaption());
                    }
                    if (archivePath == null) {
                        notSetFieldSet.add(ConfigurationParam.FORM_ARCHIVE_DIRECTORY.getCaption());
                    }
                    if (errorPath == null) {
                        notSetFieldSet.add(ConfigurationParam.FORM_ERROR_DIRECTORY.getCaption());
                    }
                }
            }
            if (departmentId == null) {
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
                if (duplicateDepartmentIdSet != null)
                    duplicateDepartmentIdSet.add(departmentId.intValue());
            }
        }
    }

    @Override
    public void onSaveClicked() {
        if (activeGroup.equals(ConfigurationParamGroup.ASYNC)) {
            onCheckAccess(true);
        } else {
            save();
        }
    }

    private void save(){
        LogCleanEvent.fire(this);
        ConfigurationParamModel model = new ConfigurationParamModel();
        SaveConfigurationAction action = new SaveConfigurationAction();
        action.setModel(model);
        // Преобразование данных таблиц в ConfigurationParamModel
        List<DataRow<Cell>> commonRowsData = rowsDataMap.get(ConfigurationParamGroup.COMMON);
        List<DataRow<Cell>> formRowsData = rowsDataMap.get(ConfigurationParamGroup.FORM);
        List<DataRow<Cell>> emailRowsData = rowsDataMap.get(ConfigurationParamGroup.EMAIL);
        List<DataRow<Cell>> asyncRowsData = rowsDataMap.get(ConfigurationParamGroup.ASYNC);

        fillModel(model, commonRowsData, formRowsData, action.getDublicateDepartmentIdSet(), action.getNotSetFields());

        // Параметры электронной почты
        List<Map<String, String>> emailParams = new ArrayList<Map<String, String>>();
        for (DataRow<Cell> dataRow : emailRowsData) {
            Map<String, String> param = new HashMap<String, String>();
            param.put(ConfigurationParamModel.EMAIL_NAME_ATTRIBUTE,
                    dataRow.getCell(getView().getEmailNameColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.EMAIL_VALUE_ATTRIBUTE,
                    dataRow.getCell(getView().getEmailValueColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.EMAIL_DESCRIPTION_ATTRIBUTE,
                    dataRow.getCell(getView().getEmailDescriptionColumn().getAlias()).getStringValue());
            emailParams.add(param);
        }
        action.setEmailParams(emailParams);

        // Параметры асинхронных задач
        List<Map<String, String>> asyncParams = new ArrayList<Map<String, String>>();
        for (DataRow<Cell> dataRow : asyncRowsData) {
            Map<String, String> param = new HashMap<String, String>();
            param.put(ConfigurationParamModel.ASYNC_TYPE_ID,
                    dataRow.getCell(getView().getAsyncTypeIdColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.ASYNC_TYPE,
                    dataRow.getCell(getView().getAsyncTypeColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.ASYNC_LIMIT_KIND,
                    dataRow.getCell(getView().getAsyncLimitKindColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.ASYNC_LIMIT,
                    dataRow.getCell(getView().getAsyncLimitColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.ASYNC_SHORT_LIMIT,
                    dataRow.getCell(getView().getAsyncShortLimitColumn().getAlias()).getStringValue());
            asyncParams.add(param);
        }

        action.setAsyncParams(asyncParams);

        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SaveConfigurationResult>() {
            @Override
            public void onSuccess(SaveConfigurationResult result) {
                /*placeManager.revealCurrentPlace();*/
            }
        }, this));
    }

    @Override
    public void onCancel() {
        getView().clearSelection();
        initRowsData(activeGroup);
        getView().setConfigData(getRowsData(activeGroup));
    }

    /**
     * Подготовка строки таблицы
     */
    private DataRow<Cell> createDataRow(ConfigurationParamGroup group) {
        DataRow<Cell> dataRow = new DataRow<Cell>();

        if (group.equals(ConfigurationParamGroup.COMMON)) {
            Cell paramCell = new Cell();
            paramCell.setColumn(getView().getParamColumn());
            paramCell.setEditable(true);

            Cell valueCell = new Cell();
            valueCell.setColumn(getView().getValueColumn());
            valueCell.setEditable(true);

            dataRow.setFormColumns(asList(paramCell, valueCell));

        } else if (group.equals(ConfigurationParamGroup.FORM)) {
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

        } else if (group.equals(ConfigurationParamGroup.EMAIL)) {
            Cell nameCell = new Cell();
            nameCell.setColumn(getView().getEmailNameColumn());
            nameCell.setEditable(false);

            Cell valueCell = new Cell();
            valueCell.setColumn(getView().getEmailValueColumn());
            valueCell.setEditable(true);

            Cell descriptionCell = new Cell();
            descriptionCell.setColumn(getView().getEmailDescriptionColumn());
            descriptionCell.setEditable(false);

            dataRow.setFormColumns(asList(nameCell, valueCell, descriptionCell));
        } else if (group.equals(ConfigurationParamGroup.ASYNC)) {
            Cell typeIdCell = new Cell();
            typeIdCell.setColumn(getView().getAsyncTypeIdColumn());
            typeIdCell.setEditable(false);

            Cell typeCell = new Cell();
            typeCell.setColumn(getView().getAsyncTypeColumn());
            typeCell.setEditable(false);

            Cell limitKindCell = new Cell();
            limitKindCell.setColumn(getView().getAsyncLimitKindColumn());
            limitKindCell.setEditable(false);

            Cell limitCell = new Cell();
            limitCell.setColumn(getView().getAsyncLimitColumn());
            limitCell.setEditable(true);

            Cell shortLimitCell = new Cell();
            shortLimitCell.setColumn(getView().getAsyncShortLimitColumn());
            shortLimitCell.setEditable(true);

            dataRow.setFormColumns(asList(typeIdCell, typeCell, limitKindCell, limitCell, shortLimitCell));
        }

        return dataRow;
    }

    @Override
    public void onAddRow() {
        Integer index = getSelectedIndex();
        List<DataRow<Cell>> data = rowsDataMap.get(activeGroup);
        if (index == null) {
            index = data.size() - 1;
        }
        data.add(index + 1, createDataRow(activeGroup));
        getView().setConfigData(data);
    }

    @Override
    public void onCheckAccess(final boolean needSaveAfter) {
        DataRow<Cell> selRow = getView().getSelectedObject();
        if (selRow == null && !(activeGroup.equals(ConfigurationParamGroup.EMAIL) || activeGroup.equals(ConfigurationParamGroup.ASYNC))) {
            return;
        }
        LogCleanEvent.fire(this);
        ConfigurationParamModel model = new ConfigurationParamModel();
        final CheckAccessAction action = new CheckAccessAction();
        action.setModel(model);
        action.setGroup(activeGroup);
        // Преобразование данных таблиц в ConfigurationParamModel
        List<DataRow<Cell>> commonRowsData = new ArrayList<DataRow<Cell>>();
        List<DataRow<Cell>> formRowsData = new ArrayList<DataRow<Cell>>();
        List<DataRow<Cell>> emailRowsData = new ArrayList<DataRow<Cell>>();
        List<DataRow<Cell>> asyncRowsData = new ArrayList<DataRow<Cell>>();
        if (activeGroup.equals(ConfigurationParamGroup.COMMON)) {
            commonRowsData.add(selRow);
        } else if (activeGroup.equals(ConfigurationParamGroup.FORM)) {
            formRowsData.add(selRow);
        }  else if (activeGroup.equals(ConfigurationParamGroup.EMAIL)) {
            emailRowsData.addAll(rowsDataMap.get(activeGroup));
        } else if (activeGroup.equals(ConfigurationParamGroup.ASYNC)) {
            asyncRowsData = rowsDataMap.get(ConfigurationParamGroup.ASYNC);
        }
        fillModel(model, commonRowsData, formRowsData, null, null);

        // Параметры электронной почты
        List<Map<String, String>> emailParams = new ArrayList<Map<String, String>>();
        for (DataRow<Cell> dataRow : emailRowsData) {
            Map<String, String> param = new HashMap<String, String>();
            param.put(ConfigurationParamModel.EMAIL_NAME_ATTRIBUTE,
                    dataRow.getCell(getView().getEmailNameColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.EMAIL_VALUE_ATTRIBUTE,
                    dataRow.getCell(getView().getEmailValueColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.EMAIL_DESCRIPTION_ATTRIBUTE,
                    dataRow.getCell(getView().getEmailDescriptionColumn().getAlias()).getStringValue());
            emailParams.add(param);
        }
        action.setEmailParams(emailParams);

        // Параметры асинхронных задач
        List<Map<String, String>> asyncParams = new ArrayList<Map<String, String>>();
        for (DataRow<Cell> dataRow : asyncRowsData) {
            Map<String, String> param = new HashMap<String, String>();
            param.put(ConfigurationParamModel.ASYNC_TYPE,
                    dataRow.getCell(getView().getAsyncTypeColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.ASYNC_LIMIT_KIND,
                    dataRow.getCell(getView().getAsyncLimitKindColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.ASYNC_LIMIT,
                    dataRow.getCell(getView().getAsyncLimitColumn().getAlias()).getStringValue());
            param.put(ConfigurationParamModel.ASYNC_SHORT_LIMIT,
                    dataRow.getCell(getView().getAsyncShortLimitColumn().getAlias()).getStringValue());
            asyncParams.add(param);
        }

        action.setAsyncParams(asyncParams);

        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<CheckAccessResult>() {
            @Override
            public void onSuccess(CheckAccessResult result) {
                if (needSaveAfter) {
                    if (result.isHasError()) {
                        LogAddEvent.fire(ConfigurationPresenter.this, result.getUuid());
                        LogShowEvent.fire(ConfigurationPresenter.this, true);
                    } else {
                        save();
                    }
                } else {
                    if (result.getUuid() != null) {
                        LogAddEvent.fire(ConfigurationPresenter.this, result.getUuid());
                        LogShowEvent.fire(ConfigurationPresenter.this, true);
                    } else {
                        getView().clearSelection();
                    }
                }
            }
        }, this));
    }

    private void initRowsData(final ConfigurationParamGroup group) {
        dispatcher.execute(new GetConfigurationAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetConfigurationResult>() {
                            @Override
                            public void onSuccess(GetConfigurationResult result) {
                                rowsDataMap.put(ConfigurationParamGroup.COMMON, getCommonRowsData(result.getModel()));
                                Collections.sort(rowsDataMap.get(group), comparatorMap.get(group));
                                rowsDataMap.put(ConfigurationParamGroup.FORM, getFormRowsData(result.getModel(),
                                        result.getDereferenceDepartmentNameMap()));
                                Collections.sort(rowsDataMap.get(group), comparatorMap.get(group));
                                rowsDataMap.put(ConfigurationParamGroup.ASYNC, getAsyncRowsData(result.getAsyncConfigs()));
                                Collections.sort(rowsDataMap.get(group), comparatorMap.get(group));
                                rowsDataMap.put(ConfigurationParamGroup.EMAIL, getEmailRowsData(result.getEmailConfigs()));
                                switch (group){
                                    case COMMON:
                                        getView().setConfigData(rowsDataMap.get(ConfigurationParamGroup.COMMON));
                                        break;
                                    case FORM:

                                        getView().setConfigData(rowsDataMap.get(ConfigurationParamGroup.FORM)) ;
                                        break;
                                    case ASYNC:
                                        getView().setConfigData(getAsyncRowsData(result.getAsyncConfigs()));
                                        break;
                                    case EMAIL:
                                        getView().setConfigData(getEmailRowsData(result.getEmailConfigs()));
                                        break;
                                }
                            }
                        }, this).addCallback(TaManualRevealCallback.create(this, placeManager)));
    }

    @Override
    public List<DataRow<Cell>> getRowsData(ConfigurationParamGroup group) {
        activeGroup = group;
        if (rowsDataMap.containsKey(group) && !rowsDataMap.get(group).isEmpty()){
            return rowsDataMap.get(group);
        } else {
            initRowsData(group);
            return new ArrayList<DataRow<Cell>>(0);
        }
    }

    @Override
    public void onDeleteItem() {
        List<DataRow<Cell>> rowsData = rowsDataMap.get(activeGroup);
        rowsData.remove(getView().getSelectedObject());
        getView().clearSelection();
        getView().setConfigData(rowsData);
    }

    private void initMaps() {
        /*tableMap.put(ConfigurationParamGroup.COMMON, commonTable);*/
        /*tableMap.put(ConfigurationParamGroup.FORM, formTable);
        tableMap.put(ConfigurationParamGroup.EMAIL, emailTable);
        tableMap.put(ConfigurationParamGroup.ASYNC, asyncTable);*/

        rowsDataMap.put(ConfigurationParamGroup.COMMON, new ArrayList<DataRow<Cell>>());
        rowsDataMap.put(ConfigurationParamGroup.FORM, new ArrayList<DataRow<Cell>>());
        rowsDataMap.put(ConfigurationParamGroup.EMAIL, new ArrayList<DataRow<Cell>>());
        rowsDataMap.put(ConfigurationParamGroup.ASYNC, new ArrayList<DataRow<Cell>>());

        comparatorMap.put(ConfigurationParamGroup.COMMON, commonComparator);
        comparatorMap.put(ConfigurationParamGroup.FORM, formComparator);
        comparatorMap.put(ConfigurationParamGroup.ASYNC, asyncComparator);
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

    private Integer getSelectedIndex() {
        DataRow<Cell> selRow = getView().getSelectedObject();
        if (selRow == null) {
            return null;
        }
        List<DataRow<Cell>> rowsData = rowsDataMap.get(activeGroup);
        return rowsData.indexOf(selRow);
    }
}
