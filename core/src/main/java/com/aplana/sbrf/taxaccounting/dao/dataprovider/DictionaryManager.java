package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import java.io.Serializable;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

/**
 * Этот класс сделан специально для того, чтобы получать {@link DictionaryDataProvider} для конкретного справочника по
 * его (справочника) коду.
 */
public interface DictionaryManager<ValueType extends Serializable> {
	/**
	 * @param dictionaryCode строковый код справочника, который задается в контексте нашем спринговом.
	 * @return {@link DictionaryDataProvider} для конкретного справочника по его коду.
	 */
	public DictionaryDataProvider<ValueType> getDataProvider(String dictionaryCode);

	/**
	 * Получить список определённых в данном менеджере справочников
	 * @return возвращает список определённых в менеджере справочников в виде списка {@link DictionaryItem},
	 * в элементах которого поле name задаёт название справочника, а value - его уникальный код 
	 */
	List<DictionaryItem<String>> getAvailableDictionaries();
}