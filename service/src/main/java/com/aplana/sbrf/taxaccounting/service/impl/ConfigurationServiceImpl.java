package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;

@Service("configurationService")
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {
    private static final int INN_JUR_LENGTH = 10;
    private static final int COMMON_PARAM_DEPARTMENT_ID = 0;
    private static final int MAX_LENGTH = 500;
    private static final int EMAIL_MAX_LENGTH = 200;
    private static final String SBERBANK_INN_DEFAULT = "7707083893";
    private static final String NO_CODE_DEFAULT = "9979";
    private static final String SHOW_TIMING_DEFAULT = "0";
    private static final String LIMIT_IDENT_DEFAULT = "0.65";
    private static final String ENABLE_IMPORT_PERSON_DEFAULT = "1";
    private static final String NOT_SET_ERROR = "Не задано значение поля «%s»!";
    private static final String DUPLICATE_SET_ERROR = "Значение «%s» уже задано!";
    private static final String READ_ERROR = "«%s»: Отсутствует доступ на чтение!";
    private static final String WRITE_ERROR = "«%s»: Отсутствует доступ на запись!";
    private static final String LOAD_WRITE_ERROR = "«%s» для «%s»: отсутствует доступ на запись!";
    private static final String READ_INFO = "«%s»: Присутствует доступ на чтение!";
    private static final String WRITE_INFO = "«%s»: Присутствует доступ на запись!";
    private static final String UNIQUE_PATH_ERROR = "«%s»: Значение параметра «%s» не может быть равно значению параметра «%s» для «%s»!";
    private static final String MAX_LENGTH_ERROR = "«%s»: Длина значения превышает максимально допустимую (%d)!";
    private static final String SIGN_CHECK_ERROR = "«%s»: значение не соответствует допустимому (0,1)!";
    private static final String NO_CODE_ERROR = "Код НО (пром.) (\"%s\") не найден в справочнике \"Налоговые инспекции\"";
    private static final String INN_JUR_ERROR = "Введен некорректный номер ИНН «%s»";
    private static final String NO_ENUM_CONSTANT = "Введен неверное название конфигурационного параметра";
    private static final String TASK_LIMIT_FIELD = "Ограничение на выполнение задачи";
    private static final String SHORT_QUEUE_LIMIT = "Ограничение на выполнение задачи в очереди быстрых задач";
    private static final String LIMIT_IDENT_ERROR = "Значение параметра должно быть от \"0\" до \"1\" и иметь не более 2-х знаков после запятой";
    private static final String ONLY_0_1_AVAILABLE_ERROR = "Допустимые значения параметра \"0\" и \"1\"";

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private AuditService auditService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private AsyncTaskDao asyncTaskDao;

    //Значение конфигурационных параметров по умолчанию
    private List<Configuration> defaultCommonParams() {
        List<Configuration> defaultCommonConfig = new ArrayList<Configuration>();
        defaultCommonConfig.add(new Configuration(ConfigurationParam.SBERBANK_INN.getCaption(), COMMON_PARAM_DEPARTMENT_ID, SBERBANK_INN_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.NO_CODE.getCaption(), COMMON_PARAM_DEPARTMENT_ID, NO_CODE_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.SHOW_TIMING.getCaption(), COMMON_PARAM_DEPARTMENT_ID, SHOW_TIMING_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.LIMIT_IDENT.getCaption(), COMMON_PARAM_DEPARTMENT_ID, LIMIT_IDENT_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.ENABLE_IMPORT_PERSON.getCaption(), COMMON_PARAM_DEPARTMENT_ID, ENABLE_IMPORT_PERSON_DEFAULT));
        return defaultCommonConfig;
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
    public ConfigurationParamModel fetchAllConfig(TAUserInfo userInfo) {
        return configurationDao.fetchAllAsModel();
    }

    @Override
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_NS', 'N_ROLE_OPER', 'F_ROLE_OPER')")
    public ConfigurationParamModel getCommonConfig(TAUserInfo userInfo) {
        return getCommonConfigUnsafe();
    }

    @Override
    public ConfigurationParamModel getCommonConfigUnsafe() {
        return configurationDao.fetchAllAsModelByGroup(ConfigurationParamGroup.COMMON_PARAM);
    }

    @Override
    public List<Map<String, String>> getEmailConfig() {
        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.EMAIL_CONFIG.getId());
        PagingParams pagingParams = null;
        PagingResult<Map<String, RefBookValue>> values = provider.getRecords(new Date(), pagingParams, null, null);
        List<Map<String, String>> params = new ArrayList<Map<String, String>>();
        for (Map<String, RefBookValue> value : values) {
            Map<String, String> record = new HashMap<String, String>();
            for (Map.Entry<String, RefBookValue> entry : value.entrySet()) {
                record.put(entry.getKey(), entry.getValue().getStringValue());
            }
            params.add(record);
        }
        return params;
    }

    @Override
    public List<Map<String, String>> getAsyncConfig() {
        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.ASYNC_CONFIG.getId());
        PagingParams pagingParams = null;
        PagingResult<Map<String, RefBookValue>> values = provider.getRecords(new Date(), pagingParams, null, null);
        List<Map<String, String>> params = new ArrayList<Map<String, String>>();
        for (Map<String, RefBookValue> value : values) {
            Map<String, String> record = new HashMap<String, String>();
            for (Map.Entry<String, RefBookValue> entry : value.entrySet()) {
                switch (entry.getValue().getAttributeType()) {
                    case NUMBER:
                        Number nValue = entry.getValue().getNumberValue();
                        record.put(entry.getKey(), nValue != null ? nValue.toString() : null);
                        break;
                    case STRING:
                        record.put(entry.getKey(), entry.getValue().getStringValue());
                        break;
                }
            }
            params.add(record);
        }
        return params;
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
    public ConfigurationParamModel fetchAllByDepartment(Integer departmentId, TAUserInfo userInfo) {
        return configurationDao.fetchAllByDepartment(departmentId);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model, List<Map<String, String>> emailConfigs, List<Map<String, String>> asyncConfigs, Logger logger) {
        if (model == null) {
            return;
        }
        // Длина
        for (ConfigurationParam parameter : model.keySet()) {
            Map<Integer, List<String>> map = model.get(parameter);
            int maxLength = parameter.getGroup().equals(ConfigurationParamGroup.EMAIL) ? EMAIL_MAX_LENGTH : MAX_LENGTH;
            if (map == null) {
                continue;
            }
            for (List<String> valueList : map.values()) {
                if (valueList == null) {
                    continue;
                }
                for (String value : valueList) {
                    // Проверка значения параметра "Проверять ЭП"
                    if (parameter.equals(ConfigurationParam.SIGN_CHECK)) {
                        signCheck(value, logger);
                    }
                    if (value != null && value.length() > maxLength) {
                        logger.error(MAX_LENGTH_ERROR, parameter.getCaption(), maxLength);
                    }
                }
            }
        }

        // Проверки общих параметров
        for (ConfigurationParam configurationParam : ConfigurationParam.values()) {
            if (configurationParam.getGroup().equals(ConfigurationParamGroup.COMMON)) {
                List<String> valuesList = model.get(configurationParam, 0);
                if (valuesList == null || valuesList.isEmpty()) {
                    // Обязательность
                    model.remove(configurationParam);
                } else {
                    if (configurationParam.isUnique() && valuesList.size() != 1) {
                        // Уникальность
                        logger.error(DUPLICATE_SET_ERROR, configurationParam.getCaption());
                    }
                }
            }
        }

        // Уникальность ТБ для параметров загрузки НФ (дубли могут проверяться только на клиенте)
        Set<Integer> departmentIdSet = new HashSet<Integer>();
        for (Map.Entry<ConfigurationParam, Map<Integer, List<String>>> entry : model.entrySet()) {
            if (entry.getKey().getGroup().equals(ConfigurationParamGroup.FORM)) {
                for (int departmentId : entry.getValue().keySet()) {
                    departmentIdSet.add(departmentId);
                }
            }
        }

        // Пути к каталогам для параметров загрузки НФ
        Set<ConfigurationParam> configurationParamSet = new HashSet<ConfigurationParam>(asList(
                ConfigurationParam.FORM_UPLOAD_DIRECTORY,
                ConfigurationParam.FORM_ARCHIVE_DIRECTORY,
                ConfigurationParam.FORM_ERROR_DIRECTORY));

        for (ConfigurationParam param : configurationParamSet) {
            for (int departmentId : departmentIdSet) {
                List<String> list = model.get(param, departmentId);
                if (list != null && !list.isEmpty()) {
                    String value = list.get(0);
                    // Проверка совпадения
                    for (ConfigurationParam otherParam : configurationParamSet) {
                        if (otherParam != param) {
                            List<String> otherList = model.get(otherParam, departmentId);
                            String otherValue = null;
                            if (otherList != null && !otherList.isEmpty()) {
                                otherValue = otherList.get(0);
                            }
                            if (value.equalsIgnoreCase(otherValue)) {
                                Department department = departmentDao.getDepartment(departmentId);
                                logger.error(UNIQUE_PATH_ERROR, value, param.getCaption(), otherParam.getCaption(),
                                        department.getName());
                                return;
                            }
                        }
                    }
                } else {
                    // Не все указаны
                    logger.error(NOT_SET_ERROR, param.getCaption());
                    return;
                }
            }
        }

        if (!logger.containsLevel(LogLevel.ERROR)) {
            saveAndLog(model, emailConfigs, asyncConfigs, userInfo);

            //Сохранение настроек почты
            RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.EMAIL_CONFIG.getId());

            List<Map<String, RefBookValue>> records = new ArrayList<Map<String, RefBookValue>>();
            for (Map<String, String> param : emailConfigs) {
                Map<String, RefBookValue> record = new HashMap<String, RefBookValue>();
                for (Map.Entry<String, String> entry : param.entrySet()) {
                    record.put(entry.getKey(), new RefBookValue(RefBookAttributeType.STRING, entry.getValue()));
                }
                records.add(record);
            }
            provider.updateRecords(userInfo, new Date(), records);

            //Сохранение настроек асинхронных задач
            provider = refBookFactory.getDataProvider(RefBook.Id.ASYNC_CONFIG.getId());

            records = new ArrayList<Map<String, RefBookValue>>();
            for (Map<String, String> param : asyncConfigs) {
                Map<String, RefBookValue> record = new HashMap<String, RefBookValue>();
                for (Map.Entry<String, String> entry : param.entrySet()) {
                    record.put(entry.getKey(), new RefBookValue(RefBookAttributeType.STRING, entry.getValue()));
                }
                records.add(record);
            }
            provider.updateRecords(userInfo, new Date(), records);
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String checkReadWriteAccess(TAUserInfo userInfo, Configuration param) {
        Logger logger = new Logger();
        if (param == null) {
            return null;
        }

        ConfigurationParam configParam = ConfigurationParam.getValueByCaption(param.getDescription());
        if (configParam == null) {
            logger.error(NO_ENUM_CONSTANT);
            return logEntryService.save(logger.getEntries());
        }
        Boolean isFolder = configParam.isFolder();
        // у папок smb в конце должен быть слеш (иначе возникенет ошибка при configurationParam.isFolder() == true и configurationParam.hasReadCheck() == true)
        if (isFolder == null) {
            FileWrapper keyResourceFolder = ResourceUtils.getSharedResource(param.getValue(), false);
            if (keyResourceFolder.isFile()) {
                isFolder = false;
            } else if (keyResourceFolder.isDirectory()) {
                isFolder = true;
                param.setValue(param.getValue() + "/");
            } else {
                isFolder = false;
            }
        } else if (isFolder) {
            param.setValue(param.getValue() + "/");
        }
        // Проверка значения параметра "Проверять ЭП"
        if (configParam.equals(ConfigurationParam.SIGN_CHECK)) {
            signCheck(param.getValue(), logger);
        }
        if (configParam.hasReadCheck() && (isFolder
                && !FileWrapper.canReadFolder(param.getValue()) || !isFolder
                && !FileWrapper.canReadFile(param.getValue()))) {
            // Доступ на чтение
            logger.error(READ_ERROR, param.getValue());
        } else if (configParam.hasWriteCheck() && (isFolder
                && !FileWrapper.canWriteFolder(param.getValue()) || !isFolder
                && !FileWrapper.canWriteFile(param.getValue()))) {
            // Доступ на запись
            logger.error(WRITE_ERROR, param.getValue());
        } else {
            if (configParam.hasReadCheck()) {
                logger.info(READ_INFO, param.getValue());
            } else if (configParam.hasWriteCheck()) {
                logger.info(WRITE_INFO, param.getValue());
            }
        }
        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void checkReadWriteAccess(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger) {
        if (model == null) {
            return;
        }

        // Проверки общих параметров
        for (ConfigurationParam configurationParam : ConfigurationParam.values()) {
            if (configurationParam.getGroup().equals(ConfigurationParamGroup.COMMON)) {
                List<String> valuesList = model.get(configurationParam, 0);
                if (valuesList != null)
                    for (String value : valuesList) {
                        Boolean isFolder = configurationParam.isFolder();
                        // у папок smb в конце должен быть слеш (иначе возникенет ошибка при configurationParam.isFolder() == true и configurationParam.hasReadCheck() == true)
                        if (isFolder == null) {
                            FileWrapper keyResourceFolder = ResourceUtils.getSharedResource(value, false);
                            if (keyResourceFolder.isFile()) {
                                isFolder = false;
                            } else if (keyResourceFolder.isDirectory()) {
                                isFolder = true;
                                value = value + "/";
                            } else {
                                isFolder = false;
                            }
                        } else if (isFolder) {
                            value = value + "/";
                        }
                        // Проверка значения параметра "Проверять ЭП"
                        if (configurationParam.equals(ConfigurationParam.SIGN_CHECK)) {
                            signCheck(value, logger);
                        }
                        if (configurationParam.hasReadCheck() && (isFolder
                                && !FileWrapper.canReadFolder(value) || !isFolder
                                && !FileWrapper.canReadFile(value))) {
                            // Доступ на чтение
                            logger.error(READ_ERROR, value);
                        } else if (configurationParam.hasWriteCheck() && (isFolder
                                && !FileWrapper.canWriteFolder(value) || !isFolder
                                && !FileWrapper.canWriteFile(value))) {
                            // Доступ на запись
                            logger.error(WRITE_ERROR, value);
                        } else {
                            if (configurationParam.hasReadCheck()) {
                                logger.info(READ_INFO, value);
                            } else if (configurationParam.hasWriteCheck()) {
                                logger.info(WRITE_INFO, value);
                            }
                        }
                    }
            }
        }

        // Уникальность ТБ для параметров загрузки НФ (дубли могут проверяться только на клиенте)
        Set<Integer> departmentIdSet = new HashSet<Integer>();
        for (Map.Entry<ConfigurationParam, Map<Integer, List<String>>> entry : model.entrySet()) {
            if (entry.getKey().getGroup().equals(ConfigurationParamGroup.FORM)) {
                for (int departmentId : entry.getValue().keySet()) {
                    departmentIdSet.add(departmentId);
                }
            }
        }

        // Пути к каталогам для параметров загрузки НФ
        Set<ConfigurationParam> configurationParamSet = new HashSet<ConfigurationParam>(asList(
                ConfigurationParam.FORM_UPLOAD_DIRECTORY,
                ConfigurationParam.FORM_ARCHIVE_DIRECTORY,
                ConfigurationParam.FORM_ERROR_DIRECTORY));

        for (ConfigurationParam param : configurationParamSet) {
            for (int departmentId : departmentIdSet) {
                List<String> list = model.get(param, departmentId);
                if (list != null && !list.isEmpty()) {
                    String value = list.get(0);
                    // Проверка доступности
                    if (!FileWrapper.canWriteFolder(value)) {
                        Department department = departmentDao.getDepartment(departmentId);
                        logger.error(LOAD_WRITE_ERROR, value, department.getName());
                    } else {
                        logger.info(WRITE_INFO, value);
                    }
                }
            }
        }
    }

    @Override
    public List<ConfigurationParam> checkCommonConfigurationParams(Map<ConfigurationParam, String> configurationParamMap, Logger logger) {
        List<ConfigurationParam> result = new ArrayList<ConfigurationParam>();

        String inn = configurationParamMap.get(ConfigurationParam.SBERBANK_INN);
        if (inn.length() != INN_JUR_LENGTH || !RefBookUtils.checkControlSumInn(inn)) {
            logger.error(INN_JUR_ERROR, inn);
            result.add(ConfigurationParam.SBERBANK_INN);
        }

        String noCode = configurationParamMap.get(ConfigurationParam.NO_CODE);
        RefBookDataProvider taxInspectionDataProvider = refBookFactory.getDataProvider(RefBook.Id.TAX_INSPECTION.getId());

        if (taxInspectionDataProvider.getRecordsCount(new Date(), "code = '" + noCode + "'") == 0) {
            logger.error(NO_CODE_ERROR, noCode);
            result.add(ConfigurationParam.NO_CODE);
        }

        return result;
    }

    @Override
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_NS', 'N_ROLE_OPER', 'F_ROLE_OPER')")
    public void saveCommonConfigurationParams(Map<ConfigurationParam, String> configurationParamMap, TAUserInfo userInfo) {
        configurationDao.update(configurationParamMap, COMMON_PARAM_DEPARTMENT_ID);
    }

    /**
     * Сохранить новые конфигурационные параметры и записать в ЖА
     *
     * @param model    модель с конфигурационными параметрами
     * @param userInfo информация о пользователе
     */
    @Deprecated
    void saveAndLog(ConfigurationParamModel model, List<Map<String, String>> emailConfigs, List<Map<String, String>> asyncConfigs, TAUserInfo userInfo) {
        ConfigurationParamModel oldModel = configurationDao.fetchAllAsModel();
        Map<String, Map<String, String>> oldEmailConfigMap = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> oldAsyncConfigMap = new HashMap<String, Map<String, String>>();
        for (Map<String, String> config : getEmailConfig()) {
            oldEmailConfigMap.put(config.get("NAME"), config);
        }
        for (Map<String, String> config : getAsyncConfig()) {
            oldAsyncConfigMap.put(config.get("ID"), config);
        }
        model.put(ConfigurationParam.NO_CODE, oldModel.get(ConfigurationParam.NO_CODE));
        model.put(ConfigurationParam.SBERBANK_INN, oldModel.get(ConfigurationParam.SBERBANK_INN));
        model.put(ConfigurationParam.LIMIT_IDENT, oldModel.get(ConfigurationParam.LIMIT_IDENT));
        model.put(ConfigurationParam.SHOW_TIMING, oldModel.get(ConfigurationParam.SHOW_TIMING));
        model.put(ConfigurationParam.ENABLE_IMPORT_PERSON, oldModel.get(ConfigurationParam.ENABLE_IMPORT_PERSON));
        configurationDao.save(model);
        for (ConfigurationParam param : ConfigurationParam.values()) {
            if (ConfigurationParamGroup.COMMON.equals(param.getGroup())) {
                if (model.keySet().contains(param) && oldModel.keySet().contains(param)) {
                    String keyFileUrlDiff = checkParams(oldModel, model, param, 0);
                    if (keyFileUrlDiff != null) {
                        auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                                userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.COMMON.getCaption() + ". Изменён параметр \"" + param.getCaption() + "\":" + keyFileUrlDiff, null);
                    }
                } else if (model.keySet().contains(param)) {
                    String stringValue = "\"" + model.getFullStringValue(param, 0) + "\"";
                    auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                            userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.COMMON.getCaption() + ". Добавлен параметр \"" + param.getCaption() + "\":" + stringValue, null);
                } else if (oldModel.keySet().contains(param)) {
                    String stringValue = "\"" + oldModel.getFullStringValue(param, 0) + "\"";
                    auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                            userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.COMMON.getCaption() + ". Удален параметр \"" + param.getCaption() + "\":" + stringValue, null);
                }
            } else if (ConfigurationParamGroup.FORM.equals(param.getGroup())) {
                Map<Integer, List<String>> values = model.get(param);
                Map<Integer, List<String>> oldValues = oldModel.get(param);
                Set<Integer> allDepartmentIds = new HashSet<Integer>();
                Set<Integer> departmentIds = values != null ? values.keySet() : new HashSet<Integer>();
                Set<Integer> oldDepartmentIds = oldValues != null ? oldValues.keySet() : new HashSet<Integer>();
                allDepartmentIds.addAll(departmentIds);
                allDepartmentIds.addAll(oldDepartmentIds);

                for (Integer departmentId : allDepartmentIds) {
                    if (departmentIds.contains(departmentId) && oldDepartmentIds.contains(departmentId)) {
                        // Проверка на изменение
                        String keyFileUrlDiff = checkParams(oldModel, model, param, departmentId);
                        if (keyFileUrlDiff != null) {
                            auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                                    departmentId, null, null, null, null, ConfigurationParamGroup.FORM.getCaption() + ". Изменён параметр \"" + param.getCaption() + "\":" + keyFileUrlDiff, null);
                        }
                    } else if (departmentIds.contains(departmentId)) {
                        // Добавление
                        String stringValue = "\"" + model.getFullStringValue(param, departmentId) + "\"";
                        auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                                departmentId, null, null, null, null, ConfigurationParamGroup.FORM.getCaption() + ". Добавлен параметр \"" + param.getCaption() + "\":" + stringValue, null);
                    } else {
                        // Удаление
                        String stringValue = "\"" + oldModel.getFullStringValue(param, departmentId) + "\"";
                        auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                                departmentId, null, null, null, null, ConfigurationParamGroup.FORM.getCaption() + ". Удален параметр \"" + param.getCaption() + "\":" + stringValue, null);
                    }
                }
            }
        }
        for (Map<String, String> config : emailConfigs) {
            Map<String, String> oldConfig = oldEmailConfigMap.get(config.get("NAME"));
            String check = checkConfig(config, oldConfig, "VALUE");
            if (check != null) {
                auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                        userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.EMAIL.getCaption() + ". Изменён параметр \"" + config.get("NAME") + "\":" + check, null);
            }
        }
        RefBook refBookAsyncConfig = refBookFactory.get(RefBook.Id.ASYNC_CONFIG.getId());
        for (Map<String, String> config : asyncConfigs) {
            Map<String, String> oldConfig = oldAsyncConfigMap.get(config.get("ID"));
            for (String key : Arrays.asList("SHORT_QUEUE_LIMIT", "TASK_LIMIT")) {
                String check = checkConfig(config, oldConfig, key);
                if (check != null) {
                    auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                            userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.ASYNC.getCaption() + ". Изменён параметр \"" + refBookAsyncConfig.getAttribute(key).getName() + "\" для задания \"" + config.get("NAME") + "\":" + check, null);
                }
            }
        }
    }

    private String checkConfig(Map<String, String> config, Map<String, String> oldConfig, String key) {
        String value = config.get(key);
        String oldValue = oldConfig.get(key);
        if (value == null) value = "";
        if (oldValue == null) oldValue = "";
        if (!value.equals(oldValue)) {
            return "\"" + oldValue + "\" -> \"" + value + "\"";
        }
        return null;
    }

    // Проверка значения параметра "Проверять ЭП"
    private void signCheck(String value, Logger logger) {
        if (!"0".equals(value) && !"1".equals(value)) {
            logger.error(SIGN_CHECK_ERROR, value);
        }
    }

    /**
     * Изменился ли путь настроики
     *
     * @param oldModel модель со старыми данными
     * @param newModel модель с новыми данными
     * @return если изменился параметр, то возвращает и старый и новый значениея
     */
    private String checkParams(ConfigurationParamModel oldModel, ConfigurationParamModel newModel, ConfigurationParam param, Integer departmentId) {
        String oldModelFullStringValue = oldModel.getFullStringValue(param, departmentId);
        String newModelFullStringValue = newModel.getFullStringValue(param, departmentId);
        if (oldModelFullStringValue != null && newModelFullStringValue != null) {
            if (!oldModelFullStringValue.equals(newModelFullStringValue)) {
                return "\"" + oldModelFullStringValue + "\" -> \"" + newModelFullStringValue + "\"";
            } else {
                return null;
            }
        } else if (oldModelFullStringValue == null && newModelFullStringValue == null) {
            return null;
        } else if (oldModelFullStringValue != null) {
            return "\"" + oldModelFullStringValue + "\" -> \"\"";
        } else {
            return "\"\" -> \"" + newModelFullStringValue + "\"";
        }
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_GENERAL_PARAMS)")
    public void setCommonParamsDefault(TAUserInfo userInfo) {
        configurationDao.update(defaultCommonParams());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public PagingResult<AsyncTaskTypeData> fetchAllAsyncParam(PagingParams pagingParams) {
        return asyncTaskDao.fetchAllAsyncTaskTypeData(pagingParams);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
    public PagingResult<Configuration> fetchAllCommonParam(PagingParams pagingParams, ConfigurationParamGroup configurationParamGroup) {
        return configurationDao.fetchAllByGroupAndPaging(configurationParamGroup, pagingParams);
    }


    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String create(Configuration commonParam, TAUserInfo userInfo) {
        Logger logger = new Logger();
        if (commonParam != null) {
            ConfigurationParam param = ConfigurationParam.getValueByCaption(commonParam.getDescription());
            if (param != null) {
                configurationDao.createCommonParam(param, commonParam.getValue());
                String message = ConfigurationParamGroup.COMMON.getCaption() + ". Добавлен параметр \"" + param.getCaption() + "\": " + commonParam.getValue();
                auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo, userInfo.getUser().getDepartmentId(), null, null,
                        null, null, message, null);
                logger.info(message);
            }

        }
        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public PagingResult<Configuration> fetchAllNonChangedCommonParam(PagingParams pagingParams) {
        List<Configuration> result = new ArrayList<>();
        for (ConfigurationParam param : ConfigurationParam.getParamsByGroup(ConfigurationParamGroup.COMMON)) {
            result.add(new Configuration(param.name(), param.getCaption()));
            for (Configuration commonParam : configurationDao.fetchAllByGroupAndPaging(ConfigurationParamGroup.COMMON, pagingParams)) {
                if (param.name().equals(commonParam.getCode())) {
                    result.remove(result.size() - 1);
                    break;
                }
            }
        }
        return new PagingResult<>(result, result.size());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String remove(List<String> names, TAUserInfo userInfo) {
        Logger logger = new Logger();
        List<ConfigurationParam> params = new ArrayList<>();
        for (String name : names) {
            ConfigurationParam param = ConfigurationParam.getValueByCaption(name);
            if (param != null) {
                params.add(param);
                String message = ConfigurationParamGroup.COMMON.getCaption() + ". Удален параметр \"" + param.getCaption();
                auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                        userInfo.getUser().getDepartmentId(), null, null, null, null, message, null);
                logger.info(message);
            }
        }
        configurationDao.removeCommonParam(params);
        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONTROL_UNP')")
    public String updateCommonParam(Configuration commonParam, TAUserInfo userInfo) {
        Logger logger = new Logger();
        checkConfig(commonParam, logger);
        if (!logger.containsLevel(LogLevel.ERROR)) {
            configurationDao.update(commonParam);
            String message = ConfigurationParamGroup.COMMON.getCaption() + ". Изменён параметр \"" + commonParam.getDescription() + "\": " + commonParam.getValue();
            auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                    userInfo.getUser().getDepartmentId(), null, null, null, null, message, null);
            logger.info(message);
        }
        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String updateAsyncParam(AsyncTaskTypeData asyncParam, TAUserInfo userInfo) {
        Logger logger = new Logger();
        AsyncTaskTypeData oldAsyncParam = asyncTaskDao.getTaskTypeData(asyncParam.getId());
        configurationDao.updateAsyncParam(asyncParam);

        String message = ConfigurationParamGroup.ASYNC.getCaption() +
                ". Изменён параметр \"" + ((!Objects.equals(asyncParam.getTaskLimit(), oldAsyncParam.getTaskLimit()) ? TASK_LIMIT_FIELD : (!Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit()) ? SHORT_QUEUE_LIMIT : "")) +
                "\" для задания \"" + oldAsyncParam.getName() + "\": " + (!Objects.equals(asyncParam.getTaskLimit(), oldAsyncParam.getTaskLimit()) ? asyncParam.getTaskLimit() :
                (!Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit()) ? asyncParam.getShortQueueLimit() : "")));

        auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null, message, null);
        logger.info(message);

        if (!Objects.equals(asyncParam.getTaskLimit(), oldAsyncParam.getTaskLimit()) && !Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit())) {

            message = ConfigurationParamGroup.ASYNC.getCaption() +
                    ". Изменён параметр \"" + (!Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit()) ? SHORT_QUEUE_LIMIT : "") +
                    "\" для задания \"" + oldAsyncParam.getName() + "\": " + (!Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit()) ? asyncParam.getShortQueueLimit() : "");

            auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                    userInfo.getUser().getDepartmentId(), null, null, null, null, message, null);
            logger.info(message);

        }

        return logEntryService.save(logger.getEntries());
    }

    /**
     * Проверка параметра на пренадлежность к {@link ConfigurationParamGroup#COMMON_PARAM}
     *
     * @param config проверяемый параметр
     * @param logger логгер
     */
    private void checkConfig(Configuration config, Logger logger) {
        if (config.getCode().equals(ConfigurationParam.SBERBANK_INN.getCaption())) {
            checkSberbankInn(config.getValue(), logger);
        } else if (config.getCode().equals(ConfigurationParam.NO_CODE.getCaption())) {
            checkNoCode(config.getValue(), logger);
        } else if (config.getCode().equals(ConfigurationParam.LIMIT_IDENT.name())) {
            checkLimitIdent(config.getValue(), logger);
        } else if (config.getCode().equals(ConfigurationParam.SHOW_TIMING.name()) ||
                config.getCode().equals(ConfigurationParam.ENABLE_IMPORT_PERSON.name())) {
            check01Value(config.getValue(), logger);
        }
    }

    /**
     * Проверка значения параметра на 0 или 1
     *
     * @param value  значение параметра
     * @param logger логгер
     */
    private void check01Value(String value, Logger logger) {
        if (!"0".equals(value) && !"1".equals(value)) {
            logger.error(ONLY_0_1_AVAILABLE_ERROR);
        }
    }

    /**
     * Проверка значения параметра {@link ConfigurationParam#LIMIT_IDENT}
     *
     * @param value  значение параметра
     * @param logger логгер
     */

    private void checkLimitIdent(String value, Logger logger) {
        try {
            BigDecimal decimal = new BigDecimal(value);
            if ((decimal.precision() > 2) || (decimal.doubleValue() < 0) || (decimal.doubleValue() > 1)) {
                logger.error(LIMIT_IDENT_ERROR);
            }
        } catch (NumberFormatException e) {
            logger.error(LIMIT_IDENT_ERROR);
        }
    }

    /**
     * Проверка значения параметра {@link ConfigurationParam#NO_CODE}
     *
     * @param noCode код параметра
     * @param logger логгер
     */
    private void checkNoCode(String noCode, Logger logger) {
        RefBookDataProvider taxInspectionDataProvider = refBookFactory.getDataProvider(RefBook.Id.TAX_INSPECTION.getId());
        if (taxInspectionDataProvider.getRecordsCount(new Date(), "code = '" + noCode + "'") == 0) {
            logger.error(NO_CODE_ERROR, noCode);
        }
    }

    /**
     * Проверка значения параметра {@link ConfigurationParam#SBERBANK_INN}
     *
     * @param inn    ИНН
     * @param logger логгер
     */
    private void checkSberbankInn(String inn, Logger logger) {
        if (inn.length() != INN_JUR_LENGTH || !RefBookUtils.checkControlSumInn(inn)) {
            logger.error(INN_JUR_ERROR, inn);
        }
    }
}
