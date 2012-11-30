package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

public interface DictionaryManager<T> {
	public List<DictionaryItem<T>> getAvailableDictionaries();
	
	public DictionaryDataProvider<T> getDataProvider(String dictionaryCode);
}
