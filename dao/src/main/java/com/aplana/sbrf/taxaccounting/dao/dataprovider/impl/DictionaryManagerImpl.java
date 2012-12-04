package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;

import java.io.Serializable;
import java.util.Map;

public class DictionaryManagerImpl<ValueType extends Serializable> implements DictionaryManager<ValueType>{
	private Map<String, DictionaryDataProvider<ValueType>> dataProviders;

	public void setDataProviders(Map<String, DictionaryDataProvider<ValueType>> dataProviders) {
		this.dataProviders = dataProviders;
	}
	
	public DictionaryDataProvider<ValueType> getDataProvider(String dictionaryCode) {
		DictionaryDataProvider<ValueType> dp = dataProviders.get(dictionaryCode);
		if (dp == null) {
			throw new DaoException("Неизвестный код справочника: " + dictionaryCode);
		}
		return dp;
	}
}
