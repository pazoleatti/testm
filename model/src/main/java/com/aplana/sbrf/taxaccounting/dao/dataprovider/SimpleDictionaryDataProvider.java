package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;

public interface SimpleDictionaryDataProvider<ValueType> {
	List<SimpleDictionaryItem<ValueType>> getValues();
}
