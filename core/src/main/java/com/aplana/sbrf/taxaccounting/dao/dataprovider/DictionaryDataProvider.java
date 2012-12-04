package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.io.Serializable;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

public interface DictionaryDataProvider<ValueType extends Serializable> {
	List<DictionaryItem<ValueType>> getValues();
	DictionaryItem<ValueType> getItem(ValueType value);
}
