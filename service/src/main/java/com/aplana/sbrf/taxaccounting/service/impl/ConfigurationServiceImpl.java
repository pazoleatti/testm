package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	private ConfigurationDao configDao;

	@Override
	public ConfigurationParamModel getAllConfig(TAUserInfo userInfo) {
		if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN)&&!userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)){
			throw new AccessDeniedException("Нет прав на просмотр конфигурационных параметров приложения");
		}
		return configDao.loadParams();
	}

	@Override
	public void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model) {
		if (!userInfo.getUser().hasRole(TARole.ROLE_ADMIN)){
			throw new AccessDeniedException("Нет прав на изменение конфигурационных параметров приложения");
		}
		configDao.saveParams(model);
	}
}
