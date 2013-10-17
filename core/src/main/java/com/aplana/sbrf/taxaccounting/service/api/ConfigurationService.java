package com.aplana.sbrf.taxaccounting.service.api;

import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

public interface ConfigurationService {
	
	Map<ConfigurationParam, String> getAllConfig(TAUserInfo userInfo);
	
	void saveAllConfig(TAUserInfo userInfo, Map<ConfigurationParam, String> configMap);

}
