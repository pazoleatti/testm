package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;

public class StringDictionaryManager {
	private Map<String, SimpleStringDictionaryDataProvider> dataProviders;

	public void setDataProviders(Map<String, SimpleStringDictionaryDataProvider> dataProviders) {
		this.dataProviders = dataProviders;
	}
	
	public List<SimpleDictionaryItem<String>> getValues(String dictionaryCode) {
		SimpleStringDictionaryDataProvider dp = dataProviders.get(dictionaryCode);
		if (dp == null) {
			throw new DaoException("Неизвестный код справочника: " + dictionaryCode);
		}
		return dp.getValues();
	}

	public SimpleDictionaryItem<String> getItem(String dictionaryCode, String value) {
		SimpleStringDictionaryDataProvider dp = dataProviders.get(dictionaryCode);
		if (dp == null) {
			throw new DaoException("Неизвестный код справочника: " + dictionaryCode);
		}
		return dp.getItem(value);
	}
	
	/**
	 * Метод возвращает список определённых в системе справочников в виде списка {@link SimpleDictionaryItem},
	 * в элементах которого поле name задаёт название справочника, а value - его уникальный код 
	 */
	public List<SimpleDictionaryItem<String>> getAvailableDictionaries() {
		List<SimpleDictionaryItem<String>> items = new ArrayList<SimpleDictionaryItem<String>>(dataProviders.size());
		for(Map.Entry<String, SimpleStringDictionaryDataProvider> entry: dataProviders.entrySet()) {
			SimpleDictionaryItem<String> item = new SimpleDictionaryItem<String>();
			item.setValue(entry.getKey());
			item.setName(entry.getValue().getDictionaryName());
			items.add(item);
		}
		return items;
	}
}
