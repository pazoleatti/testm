package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс провайдеров данных для справочников.
 *
 * Параметр filter функций getChildrenRecords, getRecords это условие фильтрации строк. Может быть не задано.
 * Фильтр строковый параметр - кусок sql запроса который добавляется в часть where, основного запроса для выборки данных из справочника.
 * Пример фильтра "ALIAS1 = ALIAS2 AND (ALIAS3 != 'groovy' or ALIAS3 LIKE 'java')"
 * Фильтр обрабатывается лексическим анализатором и парсером, для исключения sql инъекции, а так же для автоматического преобразования алиасов.
 * Поддерживаемые операторы сравнения: =, !=, like, is null, >, <
 * Строки должны быть включены в ординарные кавычки. (alias LIKE 'string')
 * Синтаксические операторы sql, такие как and, or, like, is null, регистронезависимые (like, LIKE, LiKe будут восприняты правильно)
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.07.13 11:53
 */

public interface RefBookDataProvider {

	/**
	 * Загружает данные справочника на определенную дату актуальности
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
	 * @param filter условие фильтрации строк. Может быть не задано
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams,
		String filter, RefBookAttribute sortAttribute);

	/**
	 * Загружает данные иерархического справочника на определенную дату актуальности
	 *
	 *
	 * @param parentRecordId код родительского элемента
	 * @param version дата актуальности
	 * @param pagingParams определяет параметры запрашиваемой страницы данных
	 * @param filter условие фильтрации строк
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version,
		PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

	/**
	 * По коду возвращает строку справочника
	 * @param recordId код строки справочника
	 * @return
	 */
	Map<String, RefBookValue> getRecordData(Long recordId);

	/**
	 * Возвращает список версий элементов справочника за указанный период времени
	 * @param startDate начальная дата
	 * @param endDate конечная дата
	 * @return
	 */
	List<Date> getVersions(Date startDate, Date endDate);

	/**
	 * Создает новые записи в справочнике
	 * @param version дата актуальности новых записей
	 * @param records список новых записей
	 */
	void insertRecords(Date version, List<Map<String, RefBookValue>> records);

	/**
	 * Обновляет значения в справочнике
	 * @param version задает дату актуальности
	 * @param records список обновленных записей
	 */
	void updateRecords(Date version, List<Map<String, RefBookValue>> records);

	/**
	 * Удаляет записи из справочника
	 * @param version задает дату удаления данных
	 * @param recordIds список кодов удаляемых записей. {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook#RECORD_ID_ALIAS Код записи}
	 */
	void deleteRecords(Date version, List<Long> recordIds);

    /**
     * Удаление всех записей справочника
     * @param version Дата удаления записей
     */
    void deleteAllRecords(Date version);

    /**
     * Значение справочника по Id записи и Id атрибута
     * @param recordId
     * @param attributeId
     * @return
     */
    RefBookValue getValue(Long recordId, Long attributeId);
}
