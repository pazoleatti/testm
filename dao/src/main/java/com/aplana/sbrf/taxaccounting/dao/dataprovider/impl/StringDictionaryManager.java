package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

public class StringDictionaryManager implements DictionaryManager<String>{
	private Map<String, StringDictionaryDataProvider> dataProviders;

	public void setDataProviders(Map<String, StringDictionaryDataProvider> dataProviders) {
		this.dataProviders = dataProviders;
	}
	
	public DictionaryDataProvider<String> getDataProvider(String dictionaryCode) {
		StringDictionaryDataProvider dp = dataProviders.get(dictionaryCode);
		if (dp == null) {
			throw new DaoException("Неизвестный код справочника: " + dictionaryCode);
		}
		return dp;
	}
	
	/**
	 * Метод возвращает список определённых в системе справочников в виде списка {@link DictionaryItem},
	 * в элементах которого поле name задаёт название справочника, а value - его уникальный код 
	 */
	public List<DictionaryItem<String>> getAvailableDictionaries() {
		List<DictionaryItem<String>> items = new ArrayList<DictionaryItem<String>>(dataProviders.size());
		for(Map.Entry<String, StringDictionaryDataProvider> entry: dataProviders.entrySet()) {
			DictionaryItem<String> item = new DictionaryItem<String>();
			item.setValue(entry.getKey());
			item.setName(entry.getValue().getDictionaryName());
			items.add(item);
		}
		return items;
	}
}
