package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;
import com.gwtplatform.dispatch.shared.Result;
/**
 * Результат запроса для получения элементов справочника.
 * Содержит общее количество записей в справочнике и необходимое 
 * количество элементов справочника.
 * @author Eugene Stetsenko
 *
 */
public class DictionaryPickerDataResult implements Result {
	private List<SimpleDictionaryItem<String>> dictionaryItems;
	private Integer size;
	
	public List<SimpleDictionaryItem<String>> getDictionaryItems() {
		return dictionaryItems;
	}
	
	public void setDictionaryItems(List<SimpleDictionaryItem<String>> dictionaryItems) {
		this.dictionaryItems = dictionaryItems;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
}