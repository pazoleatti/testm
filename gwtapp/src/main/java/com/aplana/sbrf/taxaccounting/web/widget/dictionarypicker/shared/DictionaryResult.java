package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.gwtplatform.dispatch.shared.Result;

import java.io.Serializable;
import java.util.List;

/**
 * Результат выполнения действия по получению значений справочника. Для всех событий справочников.
 * Содержит список значений справочника и количество значений справочника, удовлетворяющих условиям фильтрации.
 *
 * @author Vitalii Samolovskikh
 */
public class DictionaryResult<ValueType extends Serializable> implements Result {
	private List<DictionaryItem<ValueType>> dictionaryItems;
	private Long size;

	public List<DictionaryItem<ValueType>> getDictionaryItems() {
		return dictionaryItems;
	}

	public void setDictionaryItems(List<DictionaryItem<ValueType>> dictionaryItems) {
		this.dictionaryItems = dictionaryItems;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
}
