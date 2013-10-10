package com.aplana.sbrf.taxaccounting.core.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.aplana.sbrf.taxaccounting.core.api.ConfigurationProvider;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;

@Service
@Transactional(readOnly = true)
public class ConfigProviderImpl implements ConfigurationProvider{
	
	@Autowired
	private ConfigurationDao configDao;

	@Override
	public String getString(ConfigurationParam property, String defaultValue) {
		Map<ConfigurationParam, String> param = configDao.loadParams();
		String value = param.get(property.name());
		return StringUtils.hasText(value) ? value : defaultValue;
	}
	
	@Override
	public String getString(ConfigurationParam property) {
		return getString(property, null);
	}

	@Override
	public Boolean getBoolean(ConfigurationParam property) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer getInteger(ConfigurationParam property) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Long getLong(ConfigurationParam property) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Boolean getBoolean(ConfigurationParam property, Boolean defaultValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer getInteger(ConfigurationParam property, Integer defaultValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Long getLong(ConfigurationParam property, Long defaultValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
