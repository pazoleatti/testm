package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.gwtplatform.dispatch.shared.Result;

import java.io.Serializable;
import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public class DictionaryResult<ValueType extends Serializable> implements Result {
	private List<DictionaryItem<ValueType>> dictionaryItems;
	private Integer size;

	public List<DictionaryItem<ValueType>> getDictionaryItems() {
		return dictionaryItems;
	}

	public void setDictionaryItems(List<DictionaryItem<ValueType>> dictionaryItems) {
		this.dictionaryItems = dictionaryItems;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
}
