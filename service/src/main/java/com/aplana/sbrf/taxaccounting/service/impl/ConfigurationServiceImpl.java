package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;

@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	private ConfigurationDao configDao;

	@Override
	public Map<ConfigurationParam, String> getAllConfig(TAUserInfo userInfo) {
		if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN)){
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
	public void saveAllConfig(TAUserInfo userInfo, Map<ConfigurationParam, String> configMap) {
		if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN)){
			throw new AccessDeniedException("Нет прав на изменение конфигурационных параметров приложения");
		}
		configDao.saveParams(configMap);
	}

}
