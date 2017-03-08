package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Arrays.asList;

@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {
    private static final int INN_JUR_LENGTH = 10;
    private static final int COMMON_PARAM_DEPARTMENT_ID = 0;
    private static final int MAX_LENGTH = 500;
    private static final int EMAIL_MAX_LENGTH = 200;
    private static final String NOT_SET_ERROR = "Не задано значение поля «%s»!";
    private static final String DUPLICATE_SET_ERROR = "Значение «%s» уже задано!";
    private static final String ACCESS_READ_ERROR = "Нет прав на просмотр конфигурационных параметров приложения!";
    private static final String ACCESS_WRITE_ERROR = "Нет прав на изменение конфигурационных параметров приложения!";
    private static final String READ_ERROR = "«%s»: Отсутствует доступ на чтение!";
    private static final String WRITE_ERROR = "«%s»: Отсутствует доступ на запись!";
    private static final String LOAD_WRITE_ERROR = "«%s» для «%s»: отсутствует доступ на запись!";
    private static final String READ_INFO = "«%s»: Присутствует доступ на чтение!";
    private static final String WRITE_INFO = "«%s»: Присутствует доступ на запись!";
    private static final String UNIQUE_PATH_ERROR = "«%s»: Значение параметра «%s» не может быть равно значению параметра «%s» для «%s»!";
    private static final String MAX_LENGTH_ERROR = "«%s»: Длина значения превышает максимально допустимую (%d)!";
    private static final String SIGN_CHECK_ERROR = "«%s»: значение не соответствует допустимому (0,1)!";
    private static final String NO_CODE_ERROR="«%s» не найден в справочнике";
    private static final String INN_JUR_ERROR="Введен некорректные номер ИНН «%s»";

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

    @Override
    public ConfigurationParamModel getAllConfig(TAUserInfo userInfo) {
        if (!userInfo.getUser().hasRoles(TARole.N_ROLE_ADMIN, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            throw new AccessDeniedException(ACCESS_READ_ERROR);
        }
        return configurationDao.getAll();
    }

    @Override
    public ConfigurationParamModel getCommonConfig(TAUserInfo userInfo) {
        if (!userInfo.getUser().hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
                TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS,
                TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            throw new AccessDeniedException(ACCESS_READ_ERROR);
        }
        return configurationDao.getCommonConfig();
    }

    @Override
    public List<Map<String, String>> getEmailConfig() {
        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.EMAIL_CONFIG.getId());
        PagingResult<Map<String, RefBookValue>> values = provider.getRecords(new Date(), null, null, null);
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
        PagingResult<Map<String, RefBookValue>> values = provider.getRecords(new Date(), null, null, null);
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
    public ConfigurationParamModel getByDepartment(Integer departmentId, TAUserInfo userInfo) {
        if (!userInfo.getUser().hasRole(TARole.N_ROLE_ADMIN) && !userInfo.getUser().hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            throw new AccessDeniedException(ACCESS_READ_ERROR);
        }
        return configurationDao.getByDepartment(departmentId);
    }

    @Override
    public void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model, List<Map<String, String>> emailConfigs, List<Map<String, String>> asyncConfigs, Logger logger) {
        if (model == null) {
            return;
        }

        // Права
        if (!userInfo.getUser().hasRole(TARole.N_ROLE_ADMIN)) {
            throw new AccessDeniedException(ACCESS_WRITE_ERROR);
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
                    if (parameter.equals(parameter.SIGN_CHECK)) {
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
    public void checkReadWriteAccess(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger) {
        if (model == null) {
            return;
        }

        // Права
        if (!userInfo.getUser().hasRole(TARole.N_ROLE_ADMIN)) {
            throw new AccessDeniedException(ACCESS_WRITE_ERROR);
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
                        if (configurationParam.equals(configurationParam.SIGN_CHECK)) {
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
    public void saveCommonConfigurationParams(Map<ConfigurationParam, String> configurationParamMap, TAUserInfo userInfo) {
        if (!userInfo.getUser().hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
                TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS,
                TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            throw new AccessDeniedException(ACCESS_WRITE_ERROR);
        }
        configurationDao.update(configurationParamMap, COMMON_PARAM_DEPARTMENT_ID);
    }

    /**
     * Сохранить новые конфигурационные параметры и записать в ЖА
     *
     * @param model    модель с конфигурационными параметрами
     * @param userInfo информация о пользователе
     */
    void saveAndLog(ConfigurationParamModel model, List<Map<String, String>> emailConfigs, List<Map<String, String>> asyncConfigs, TAUserInfo userInfo) {
        ConfigurationParamModel oldModel = configurationDao.getAll();
        Map<String, Map<String, String>> oldEmailConfigMap = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> oldAsyncConfigMap = new HashMap<String, Map<String, String>>();
        for (Map<String, String> config: getEmailConfig()) {
            oldEmailConfigMap.put(config.get("NAME"), config);
        }
        for (Map<String, String> config: getAsyncConfig()) {
            oldAsyncConfigMap.put(config.get("ID"), config);
        }
        model.put(ConfigurationParam.NO_CODE, oldModel.get(ConfigurationParam.NO_CODE));
        model.put(ConfigurationParam.SBERBANK_INN, oldModel.get(ConfigurationParam.SBERBANK_INN));
        configurationDao.save(model);
        for (ConfigurationParam param: ConfigurationParam.values()) {
            if (ConfigurationParamGroup.COMMON.equals(param.getGroup())) {
                if (model.keySet().contains(param) && oldModel.keySet().contains(param)) {
                    String keyFileUrlDiff = checkParams(oldModel, model, param, 0);
                    if (keyFileUrlDiff != null) {
                        auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                                userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.COMMON.getCaption() + ". Изменён параметр \"" + param.getCaption() +"\":" + keyFileUrlDiff, null);
                    }
                } else if (model.keySet().contains(param)) {
                    String stringValue = "\"" + model.getFullStringValue(param, 0) + "\"";
                    auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                            userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.COMMON.getCaption() + ". Добавлен параметр \"" + param.getCaption() +"\":" + stringValue, null);
                } else if (oldModel.keySet().contains(param)) {
                    String stringValue = "\"" + oldModel.getFullStringValue(param, 0) + "\"";
                    auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                            userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.COMMON.getCaption() + ". Удален параметр \"" + param.getCaption() +"\":" + stringValue, null);
                }
            } else if (ConfigurationParamGroup.FORM.equals(param.getGroup())) {
                Map<Integer, List<String>> values = model.get(param);
                Map<Integer, List<String>> oldValues = oldModel.get(param);
                Set<Integer> allDepartmentIds = new HashSet<Integer>();
                Set<Integer> departmentIds = values!= null?values.keySet():new HashSet<Integer>();
                Set<Integer> oldDepartmentIds = oldValues!= null?oldValues.keySet():new HashSet<Integer>();
                allDepartmentIds.addAll(departmentIds);
                allDepartmentIds.addAll(oldDepartmentIds);

                for(Integer departmentId: allDepartmentIds) {
                    if (departmentIds.contains(departmentId) && oldDepartmentIds.contains(departmentId)) {
                        // Проверка на изменение
                        String keyFileUrlDiff = checkParams(oldModel, model, param, departmentId);
                        if (keyFileUrlDiff != null) {
                            auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                                    departmentId, null, null, null, null, ConfigurationParamGroup.FORM.getCaption() + ". Изменён параметр \"" +param.getCaption() +"\":" + keyFileUrlDiff, null);
                        }
                    } else if (departmentIds.contains(departmentId)) {
                        // Добавление
                        String stringValue = "\"" + model.getFullStringValue(param, departmentId) + "\"";
                        auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                                departmentId, null, null, null, null, ConfigurationParamGroup.FORM.getCaption() + ". Добавлен параметр \"" +param.getCaption() +"\":" + stringValue, null);
                    } else {
                        // Удаление
                        String stringValue = "\"" + oldModel.getFullStringValue(param, departmentId) + "\"";
                        auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                                departmentId, null, null, null, null, ConfigurationParamGroup.FORM.getCaption() + ". Удален параметр \"" +param.getCaption() +"\":" + stringValue, null);
                    }
                }
            }
        }
        for (Map<String, String> config: emailConfigs) {
            Map<String, String> oldConfig = oldEmailConfigMap.get(config.get("NAME"));
            String check = checkConfig(config, oldConfig, "VALUE");
            if (check != null) {
                auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                        userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.EMAIL.getCaption() + ". Изменён параметр \"" +config.get("NAME") +"\":" + check, null);
            }
        }
        RefBook refBookAsyncConfig = refBookFactory.get(RefBook.Id.ASYNC_CONFIG.getId());
        for (Map<String, String> config: asyncConfigs) {
            Map<String, String> oldConfig = oldAsyncConfigMap.get(config.get("ID"));
            for (String key: Arrays.asList("SHORT_QUEUE_LIMIT", "TASK_LIMIT")) {
                String check = checkConfig(config, oldConfig, key);
                if (check != null) {
                    auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                            userInfo.getUser().getDepartmentId(), null, null, null, null, ConfigurationParamGroup.ASYNC.getCaption() + ". Изменён параметр \"" + refBookAsyncConfig.getAttribute(key).getName() + "\" для задания \"" + config.get("NAME")+ "\":" + check, null);
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
}
