package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
	 * Импортирует данные справочника
	 * TODO: Метод временно размещается здесь, пока ему не найдется более подхлдящего места.
	 * Я думаю это должен быть ещё один сервис.
	 * 
	 * @param refBookId код справочника
	 * @return
	 */
	public void importRefBook(TAUserInfo userInfo, Logger logger, Long refBookId, InputStream is);

	/**
	 * Загружает список всех справочников
	 * @return
	 */
	List<RefBook> getAll();

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
