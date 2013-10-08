package com.aplana.sbrf.taxaccounting.refbook;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Фабрика для получения адаптеров справочников
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.07.13 11:49
 */

@ScriptExposed
public interface RefBookFactory {

	/**
	 * Загружает метаданные справочника
	 * @param refBookId код справочника
	 * @return
	 */
	RefBook get(Long refBookId);

	/**
	 * Загружает список всех справочников
	 * @param onlyVisible true - только видимые; false - весь список
	 * @return
	 */
	List<RefBook> getAll(boolean onlyVisible);

	/**
	 * Ищет справочник по коду атрибута
	 * @param attributeId код атрибута, входящего в справочник
	 * @return
	 */
	RefBook getByAttribute(Long attributeId);

	/**
	 * Возвращает провайдер данных для конкретного справочника
	 * @param refBookId код справочника
	 * @return провайдер данных
	 */
	RefBookDataProvider getDataProvider(Long refBookId);

}
