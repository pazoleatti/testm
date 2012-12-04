package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.io.Serializable;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

public interface DictionaryDataProvider<ValueType extends Serializable> {
	public List<DictionaryItem<ValueType>> getValues();
	public List<DictionaryItem<ValueType>> getValues(String pattern);
	public DictionaryItem<ValueType> getItem(ValueType value);
}
