package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;

public interface DictionaryManager<T> {
	public List<SimpleDictionaryItem<T>> getAvailableDictionaries();
	
	public SimpleDictionaryDataProvider<T> getDataProvider(String dictionaryCode);
}
