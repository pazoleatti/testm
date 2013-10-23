package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {
	
	public static final int MAX_SIZE = 255;
	
	public static final String MSG_WRONG_SIZE = "В поле '%s' превышено ограничение на длину значения. Указано %s при ограничении %s символов";

	@Autowired
	private ConfigurationDao configDao;

	@Override
	public Map<ConfigurationParam, String> getAllConfig(TAUserInfo userInfo) {
		if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN)&&!userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)){
			throw new AccessDeniedException("Нет прав на просмотр конфигурационных параметров приложения");
		}
		Map<ConfigurationParam, String> result = new HashMap<ConfigurationParam, String>();
		Map<ConfigurationParam, String> existsParam = configDao.loadParams();
		for (ConfigurationParam property : ConfigurationParam.values()) {
			String value = existsParam.get(property);
			result.put(property, value);
		}
		return result;
	}

	@Override
	public void saveAllConfig(TAUserInfo userInfo, Logger logger, Map<ConfigurationParam, String> configMap) {
		if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN)){
			throw new AccessDeniedException("Нет прав на изменение конфигурационных параметров приложения");
		}
		validation(logger, configMap);
		if (logger.containsLevel(LogLevel.ERROR)){
			throw new ServiceLoggerException("Значения параметров не валидны", logger.getEntries());
		}
		configDao.saveParams(configMap);
	}
	
	private void validation(Logger logger, Map<ConfigurationParam, String> configMap){
		for (Entry<ConfigurationParam, String> entry : configMap.entrySet()) {
			String value = entry.getValue();
			if (value != null && value.length() > MAX_SIZE){
				logger.error(MSG_WRONG_SIZE, entry.getKey().getCaption(), value.length(), MAX_SIZE);
			}
		}
	}

}
