package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

import java.io.Serializable;
import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public interface StringFilterDictionaryDataProvider<ValueType extends Serializable> {
	public List<DictionaryItem<ValueType>> getValues(String valuePattern);
}
