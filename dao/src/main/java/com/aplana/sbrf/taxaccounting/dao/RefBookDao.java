package com.aplana.sbrf.taxaccounting.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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
	 * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
	 * @param filter условие фильтрации строк. Может быть не задано
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

	/**
	 * Загружает данные иерархического справочника на определенную дату актуальности
	 *
	 *
	 * @param refBookId код справочника
	 * @param parentRecordId код родительского элемента
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных
	 * @param filter условие фильтрации строк
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, Long parentRecordId, Date version,
															   PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

	/**
	 * По коду возвращает строку справочника
	 * @param refBookId код справочника
	 * @param recordId код строки справочника
	 * @return
	 */
	Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);

	/**
	 * Возвращает список версий элементов справочника за указанный период времени
	 * @param startDate начальная дата
	 * @param endDate конечная дата
	 * @return
	 */
	List<Date> getVersions(Long refBookId, Date startDate, Date endDate);
}
