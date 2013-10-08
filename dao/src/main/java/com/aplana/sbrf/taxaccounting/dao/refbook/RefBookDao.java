package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
	 * Загружает список всех справочников
	 * @return
	 */
	List<RefBook> getAllVisible();

	/**
	 * Ищет справочник по коду атрибута
	 * @param attributeId код атрибута, входящего в справочник
	 * @return
	 */
	RefBook getByAttribute(long attributeId);

	/**
	 * Загружает данные справочника на определенную дату актуальности
	 * @param refBookId код справочника
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
	 * @param filter условие фильтрации строк. Может быть не задано
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
		String filter, RefBookAttribute sortAttribute);

	/**
	 * Загружает данные иерархического справочника на определенную дату актуальности
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

	/**
	 * Создает новые записи в справочнике
	 * @param refBookId код справочника
	 * @param version дата актуальности новых записей
	 * @param records список новых записей
	 */
	void createRecords(Long refBookId, Date version, List<Map<String, RefBookValue>> records);

	/**
	 * Обновляет значения в справочнике
	 * @param refBookId код справочника
	 * @param version задает дату актуальности
	 * @param records список обновленных записей
	 */
	void updateRecords(Long refBookId, Date version, List<Map<String, RefBookValue>> records);

	/**
	 * Удаляет записи из справочника
	 * @param refBookId код справочника
	 * @param version задает дату удаления данных
	 * @param recordIds список кодов удаляемых записей. {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook#RECORD_ID_ALIAS Код записи}
	 */
	void deleteRecords(Long refBookId, Date version, List<Long> recordIds);

    /**
     * Удаление всех записей справочника.<br>
     * Записи ближайшей меньшей версии будут отмечены как удаленные на дату удаления
     *
     * @param refBookId Id справочника
     * @param version Дата удаления записей
     */
    void deleteAllRecords(Long refBookId, Date version);

    /**
     * Проверка и поиск Id записи по:
     * @param refBookId Id справочника
     * @param version Версия
     * @param rowId Id строки справочника
     * @return Id первой найденной записи
     */
    Long checkRecordUnique(Long refBookId, Date version, Long rowId);

    /**
     * Значение справочника по Id записи и Id атрибута
     * @param recordId
     * @param attributeId
     * @return
     */
    RefBookValue getValue(Long recordId, Long attributeId);
}
