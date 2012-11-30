package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

public interface DictionaryDataProvider<ValueType> {
	List<DictionaryItem<ValueType>> getValues(String valuePattern);
	DictionaryItem<ValueType> getItem(ValueType value);
}
