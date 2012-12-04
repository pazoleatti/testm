package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.io.Serializable;

public interface DictionaryManager<ValueType extends Serializable> {
	public DictionaryDataProvider<ValueType> getDataProvider(String dictionaryCode);
}
