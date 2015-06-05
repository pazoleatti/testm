package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Arrays.asList;

@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {
    private final static int MAX_LENGTH = 500;
    private final static int EMAIL_MAX_LENGTH = 200;
    private final static String NOT_SET_ERROR = "Не задано значение поля «%s»!";
    private final static String DUPLICATE_SET_ERROR = "Значение «%s» уже задано!";
    private final static String ACCESS_READ_ERROR = "Нет прав на просмотр конфигурационных параметров приложения!";
    private final static String ACCESS_WRITE_ERROR = "Нет прав на изменение конфигурационных параметров приложения!";
    private final static String READ_ERROR = "«%s»: Отсутствует доступ на чтение!";
    private final static String WRITE_ERROR = "«%s»: Отсутствует доступ на запись!";
    private final static String LOAD_WRITE_ERROR = "«%s» для «%s»: отсутствует доступ на запись!";
    private final static String READ_INFO = "«%s»: Присутствует доступ на чтение!";
    private final static String WRITE_INFO = "«%s»: Присутствует доступ на запись!";
    private final static String UNIQUE_PATH_ERROR = "«%s»: Значение параметра «%s» не может быть равно значению параметра «%s» для «%s»!";
    private final static String MAX_LENGTH_ERROR = "«%s»: Длина значения превышает максимально допустимую (%d)!";
    private final static String SIGN_CHECK_ERROR = "«%s»: значение не соответствует допустимому (0,1)!";

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    public ConfigurationParamModel getAllConfig(TAUserInfo userInfo) {
        if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN) && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            throw new AccessDeniedException(ACCESS_READ_ERROR);
        }
        ConfigurationParamModel model = configurationDao.getAll();
        return model;
    }

    @Override
    public List<Map<String, String>> getEmailConfig() {
        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.EMAIL_CONFIG);
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
        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.ASYNC_CONFIG);
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
        if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN) && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
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
        if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN)) {
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
                    // Проверка значения параметра "Проверять ЭЦП"
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
            configurationDao.save(model);

            //Сохранение настроек почты
            RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.EMAIL_CONFIG);

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
            provider = refBookFactory.getDataProvider(RefBook.ASYNC_CONFIG);

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
        if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN)) {
            throw new AccessDeniedException(ACCESS_WRITE_ERROR);
        }

        // Проверки общих параметров
        for (ConfigurationParam configurationParam : ConfigurationParam.values()) {
            if (configurationParam.getGroup().equals(ConfigurationParamGroup.COMMON)) {
                List<String> valuesList = model.get(configurationParam, 0);
                if (valuesList != null)
                    for (String value : valuesList) {
                        // у папок smb в конце должен быть слеш (иначе возникенет ошибка при configurationParam.isFolder() == true и configurationParam.hasReadCheck() == true)
                        if (configurationParam.isFolder()) {
                            value = value + "/";
                        }
                        // Проверка значения параметра "Проверять ЭЦП"
                        if (configurationParam.equals(configurationParam.SIGN_CHECK)) {
                            signCheck(value, logger);
                        }
                        if (configurationParam.hasReadCheck() && (configurationParam.isFolder()
                                && !FileWrapper.canReadFolder(value) || !configurationParam.isFolder()
                                && !FileWrapper.canReadFile(value))) {
                            // Доступ на чтение
                            logger.error(READ_ERROR, value);
                        } else if (configurationParam.hasWriteCheck() && (configurationParam.isFolder()
                                && !FileWrapper.canWriteFolder(value) || !configurationParam.isFolder()
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

    // Проверка значения параметра "Проверять ЭЦП"
    private void signCheck(String value, Logger logger) {
        if (!"0".equals(value) && !"1".equals(value)) {
            logger.error(SIGN_CHECK_ERROR, value);
        }
    }
}
