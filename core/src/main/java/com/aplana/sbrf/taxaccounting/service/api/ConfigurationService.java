package com.aplana.sbrf.taxaccounting.service.api;

import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;

public interface ConfigurationService {
	
	Map<ConfigurationParam, String> getAllConfig();
	
	void saveAllConfig(Map<ConfigurationParam, String> configMap);

}
