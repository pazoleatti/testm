package com.aplana.sbrf.taxaccounting.dao.dataprovider;

import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.io.Serializable;

/**
 * Этот класс сделан специально для того, чтобы получать {@link DictionaryDataProvider} для конкретного справочника по
 * его (справочника) коду.
 */
public interface DictionaryManager<ValueType extends Serializable> extends ScriptExposed {
	/**
	 * @param dictionaryCode строковый код справочника, который задается в контексте нашем спринговом.
	 * @return {@link DictionaryDataProvider} для конкретного справочника по его коду.
	 */
	public DictionaryDataProvider<ValueType> getDataProvider(String dictionaryCode);
}