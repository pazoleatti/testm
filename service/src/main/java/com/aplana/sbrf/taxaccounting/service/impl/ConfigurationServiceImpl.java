package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
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
    private final static String NOT_SET_ERROR = "Не задано значение поля «%s»!";
    private final static String DUBLICATE_SET_ERROR = "Значение «%s» уже задано!";
    private final static String ACCESS_READ_ERROR = "Нет прав на просмотр конфигурационных параметров приложения!";
    private final static String ACCESS_WRITE_ERROR = "Нет прав на изменение конфигурационных параметров приложения!";
    private final static String READ_ERROR = "«%s»: Отсутствует доступ на чтение!";
    private final static String WRITE_ERROR = "«%s»: Отсутствует доступ на запись!";
    private final static String READ_INFO = "«%s»: Присутствует доступ на чтение!";
    private final static String WRITE_INFO = "«%s»: Присутствует доступ на запись!";
    private final static String UNIQUE_PATH_ERROR = "«%s»: Значение параметра «%s» не может быть равно значению параметра «%s» для «%s»!";
    private final static String MAX_LENGTH_ERROR = "«%s»: Длина значения превышает максимально допустимую (" + MAX_LENGTH + ")!";

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private DepartmentDao departmentDao;

    @Override
    public ConfigurationParamModel getAllConfig(TAUserInfo userInfo) {
        if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN) && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            throw new AccessDeniedException(ACCESS_READ_ERROR);
        }
        return configurationDao.getAll();
    }

    @Override
    public ConfigurationParamModel getByDepartment(Integer departmentId, TAUserInfo userInfo) {
        if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN) && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            throw new AccessDeniedException(ACCESS_READ_ERROR);
        }
        return configurationDao.getByDepartment(departmentId);
    }

    @Override
    public void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger) {
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
            if (map == null) {
                continue;
            }
            for (List<String> valueList : map.values()) {
                if  (valueList == null) {
                    continue;
                }
                for (String value : valueList) {
                    if (value != null && value.length() > MAX_LENGTH) {
                        logger.error(MAX_LENGTH_ERROR, parameter.getCaption());
                    }
                }
            }
        }

        // Проверки общих параметров
        for (ConfigurationParam configurationParam : ConfigurationParam.values()) {
            if (configurationParam.isCommon()) {
                List<String> valuesList = model.get(configurationParam, 0);
                if (valuesList == null || valuesList.isEmpty()) {
                    // Обязательность
                    logger.error(NOT_SET_ERROR, configurationParam.getCaption());
                } else {
                    if (configurationParam.isUnique() && valuesList.size() != 1) {
                        // Уникальность
                        logger.error(DUBLICATE_SET_ERROR, configurationParam.getCaption());
                    }
                }
            }
        }

        // Уникальность ТБ для параметров загрузки НФ (дубли могут проверяться только на клиенте)
        Set<Integer> departmentIdSet = new HashSet<Integer>();
        for (Map.Entry<ConfigurationParam, Map<Integer, List<String>>> entry : model.entrySet()) {
            if (entry.getKey().isCommon()) {
                // Общие параметры не проверяются
                continue;
            }
            for (int departmentId : entry.getValue().keySet()) {
                departmentIdSet.add(departmentId);
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
            if (configurationParam.isCommon()) {
                List<String> valuesList = model.get(configurationParam, 0);
                if (valuesList != null)
                    for (String value : valuesList) {
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
            if (entry.getKey().isCommon()) {
                // Общие параметры не проверяются
                continue;
            }
            for (int departmentId : entry.getValue().keySet()) {
                departmentIdSet.add(departmentId);
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
                        logger.error(WRITE_ERROR, value);
                    } else {
                        logger.info(WRITE_INFO, value);
                    }
                }
            }
        }
    }
}
