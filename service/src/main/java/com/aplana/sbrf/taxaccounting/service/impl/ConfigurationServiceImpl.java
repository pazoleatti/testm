package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

	private final static String ACCESS_READ_ERROR = "Нет прав на просмотр конфигурационных параметров приложения!";
    private final static String ACCESS_WRITE_ERROR = "Нет прав на изменение конфигурационных параметров приложения!";
    private final static String UNIQUE_PATH_ERROR = "«%s»: Значение параметра «%s» не может быть равно значению параметра «%s» для «%s»!";
    private final static String NOT_SET_ERROR = "%s не указан!";
    private final static String WRITE_ERROR = "«%s»: Отсутствует доступ на запись!";

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
	public void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger) {
        if (model == null) {
            return;
        }

        // Права
		if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN)){
			throw new AccessDeniedException(ACCESS_WRITE_ERROR);
		}

        // Уникальность ТБ для параметров загрузки НФ
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
                ConfigurationParam.UPLOAD_DIRECTORY,
                ConfigurationParam.ARCHIVE_DIRECTORY,
                ConfigurationParam.ERROR_DIRECTORY));

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

                   // Проверка доступности
                   try {
                       FileWrapper folder = ResourceUtils.getSharedResource(value);
                       if (folder.isFile() || !folder.canWrite()) {
                           logger.error(WRITE_ERROR, value);
                           return;
                       }
                   } catch (Exception e) {
                       logger.error(WRITE_ERROR, value);
                       return;
                   }
               } else {
                   // Не все указаны
                   logger.error(NOT_SET_ERROR, param.getCaption());
                   return;
               }
           }
        }
        configurationDao.save(model);
	}
}
