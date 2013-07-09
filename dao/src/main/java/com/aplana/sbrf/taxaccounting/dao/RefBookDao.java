package com.aplana.sbrf.taxaccounting.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

/**
 * Дао для версионных справочников.
 * <br />
 * При получении данные справочника оформляются в виде списка строк. Каждая строка
 * представляет собой набор пар "псевдоним атрибута"-"значение справочника". В списке атрибутов есть предопределенный -
 * это "id" - уникальный код строки. В рамках одного справочника псевдонимы повторяться не могут.
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 04.07.13 12:25
 */

public interface RefBookDao {

	/**
	 * Загружает метаданные справочника
	 * @param id код справочника
	 * @return
	 */
	RefBook get(Long id);

	/**
	 * Загружает список всех справочников
	 * @return
	 */
	List<RefBook> getAll();

	/**
	 * Загружает данные справочника на определенную дату актуальности
	 * @param refBookId код справочника
	 * @param version дата актуальности
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	List<Map<String, RefBookValue>> getData(Long refBookId, Date version, RefBookAttribute sortAttribute);

	/**
	 * По коду возвращает строку справочника
	 * @param refBookId код справочника
	 * @param recordId код строки справочника
	 * @return
	 */
	Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);
}
