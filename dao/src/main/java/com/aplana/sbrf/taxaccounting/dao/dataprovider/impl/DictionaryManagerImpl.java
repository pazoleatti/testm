package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

/**
 * Этот класс сделан специально для того, чтобы получать {@link DictionaryDataProvider} для конкретного справочника по
 * его (справочника) коду.
 */
public class DictionaryManagerImpl<ValueType extends Serializable> implements DictionaryManager<ValueType>{
	private Map<String, DictionaryDataProvider<ValueType>> dataProviders;

	public void setDataProviders(Map<String, DictionaryDataProvider<ValueType>> dataProviders) {
		this.dataProviders = dataProviders;
	}

	/**
	 * @param dictionaryCode строковый код справочника, который задается в контексте нашем спринговом.
	 * @return {@link DictionaryDataProvider} для конкретного справочника по его коду.
	 */
	public DictionaryDataProvider<ValueType> getDataProvider(String dictionaryCode) {
		DictionaryDataProvider<ValueType> dp = dataProviders.get(dictionaryCode);
		if (dp == null) {
			throw new DaoException("Неизвестный код справочника: " + dictionaryCode);
		}
		return dp;
	}
	
	@Override
	public List<DictionaryItem<String>> getAvailableDictionaries() {
		List<DictionaryItem<String>> items = new ArrayList<DictionaryItem<String>>(dataProviders.size());
		for(Map.Entry<String, DictionaryDataProvider<ValueType>> entry: dataProviders.entrySet()) {
			DictionaryItem<String> item = new DictionaryItem<String>();
			item.setValue(entry.getKey());
			item.setName(entry.getValue().getDictionaryName());
			items.add(item);
		}
		return items;
	}
}
