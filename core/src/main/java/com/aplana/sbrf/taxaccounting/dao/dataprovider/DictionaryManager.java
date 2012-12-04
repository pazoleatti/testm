package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.io.Serializable;

public interface DictionaryManager {
	public <ValueType extends Serializable> DictionaryDataProvider<ValueType> getDataProvider(String dictionaryCode);
}
