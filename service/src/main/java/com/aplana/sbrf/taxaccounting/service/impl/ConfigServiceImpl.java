package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;

@Service
@Transactional
public class ConfigServiceImpl implements ConfigurationService {

	@Autowired
	private ConfigurationDao configDao;

	@Override
	public Map<ConfigurationParam, String> getAllConfig() {
		Map<ConfigurationParam, String> result = new HashMap<ConfigurationParam, String>();
		Map<ConfigurationParam, String> existsParam = configDao.loadParams();
		for (ConfigurationParam property : ConfigurationParam.values()) {
			String value = existsParam.get(property.name());
			result.put(property, value);
		}
		return result;
	}

	@Override
	public void saveAllConfig(Map<ConfigurationParam, String> configMap) {
		configDao.saveParams(configMap);
	}

}
