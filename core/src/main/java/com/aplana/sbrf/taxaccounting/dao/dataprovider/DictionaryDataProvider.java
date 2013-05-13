package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.io.Serializable;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.PaginatedSearchParams;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

/**
 * Поставщик данных для справочника.
 */
public interface DictionaryDataProvider<ValueType extends Serializable> {

	/**
	 * Возвращает наименование справочника
	 * @return понятное пользователю наименование справочника
	 */
	String getDictionaryName();
	
	/**
	 * Возвращает все значения из справочника.
	 * @return список значений справочника
	 */
	List<DictionaryItem<ValueType>> getValues();

	/**
	 * Возвращает отфильтрованные значения из справочника. В качестве фильтра выступает паттерн поиска. Ищутся его
	 * вхождения как в значении из справочника, так и в описании (name) этого значения.
	 * @param pattern паттерн поиска, если null, то фильтрация не производится
	 * @param pageParams {@link PaginatedSearchParams параметры} для выбора диапазона записей
	 * @return отфильтрованный список значений справочника
	 */	
	PaginatedSearchResult<DictionaryItem<ValueType>> getValues(String pattern, PaginatedSearchParams pageParams);

	/**
	 * Возвращает запись св справочнике по значению. Запись может содержать так же и название значения.
	 * Т.е. кроме кода ОКАТО, например, ещё и его название.
	 *
	 * @param value значение из справочника
	 * @return запись в справочнике
	 */
	DictionaryItem<ValueType> getItem(ValueType value);
}
