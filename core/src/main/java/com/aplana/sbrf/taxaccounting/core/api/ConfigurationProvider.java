package com.aplana.sbrf.taxaccounting.core.api;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;


public interface ConfigurationProvider {	
	
	/**
	 * @param property параметр
	 * @return значение параметра приведенное к Boolean
	 */
	Boolean getBoolean(ConfigurationParam property);	
	
	/**
	 * @param property параметр
	 * @return значение параметра приведенное к Integer
	 */
	Integer getInteger(ConfigurationParam property);

	/**
	 * @param property параметр
	 * @return значение параметра приведенное к Long
	 */
	Long getLong(ConfigurationParam property);

	
	/**
	 * @param property параметр
	 * @return значение параметра приведенное к String
	 */
	String getString(ConfigurationParam property);	
	
	/**
	 * @param property параметр
	 * @param defaultValue значение по умолчанию
	 * @return значение параметра приведенное к Boolean
	 */
	Boolean getBoolean(ConfigurationParam property, Boolean defaultValue);	

	/**
	 * @param property параметр
	 * @param defaultValue значение по умолчанию
	 * @return значение параметра приведенное к Integer
	 */
	Integer getInteger(ConfigurationParam property, Integer defaultValue);
	
	/**
	 * @param property параметр
	 * @param defaultValue значение по умолчанию
	 * @return значение параметра приведенное к Long
	 */
	Long getLong(ConfigurationParam property, Long defaultValue);
	
	/**
	 * @param property параметр
	 * @param defaultValue значение по умолчанию
	 * @return значение параметра приведенное к String
	 */
	String getString(ConfigurationParam property, String defaultValue);
	
}
