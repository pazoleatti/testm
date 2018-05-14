package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;
import java.util.Map;

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
	 * Возвращяет все справочники
	 */
	List<RefBook> fetchAll();

	/**
	 * Загружает список всех справочников
	 *
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

    /**
     * Метод возвращает строку для фильтрации справочника по
     * определенному строковому ключу
     *
     * @param query - ключ для поиска
     * @param refBookId - справочник
     * @param exactSearch - точное соответствие
     *
     * @return строку фильтрации для поиска по справочнику
     */
    String getSearchQueryStatement(String query, Long refBookId, boolean exactSearch);

	/**
	 * Метод возвращает строку для фильтрации справочника по
	 * определенному строковому ключу и дополнительным строковым параметрам присоединенным с условием "И"
	 * @param parameters - дополнительные строковые параметры, ключ - имя поля в таблице, значение поля в таблице
	 * @param refBookId - справочник
	 * @param exactSearch - точное соответствие
	 *
	 * @return строку фильтрации для поиска по справочнику
	 */
	String getSearchQueryStatementWithAdditionalStringParameters(Map<String, String> parameters, String searchPattern, Long refBookId, boolean exactSearch);

    String getSearchQueryStatement(String query, Long refBookId);

    /**
     * Получает описание справочника
     * @param descriptionTemplate шаблон описания
     * @param refBookId идентификатор справочника
     */
    String getRefBookDescription(DescriptionTemplate descriptionTemplate, Long refBookId);

    /**
     * Получить список доступных отчётов
     * @param refBookId
     * @param userInfo
     * @param logger
     * @return
     */
    List<String> getSpecificReportTypes(long refBookId, TAUserInfo userInfo, Logger logger);

    boolean getEventScriptStatus(long refBookId, FormDataEvent event);

    /**
     * Статус возможности обработки события FormDataEvent скриптом макета
     * @param refBookId
     * @return
     */
    Map<FormDataEvent, Boolean> getEventScriptStatus(long refBookId);

    /**
     * Формирует ключ блокировки для задачи
     * @param refBookId
     * @return
     */
    String generateTaskKey(long refBookId);

	/**
	 * Возвращает описание блокировки для справочника
	 * @param lockData данные блокировки
	 * @param refBookId идентификатор справочника
	 * @return информация о блокировке с деталями
     */
	String getRefBookLockDescription(LockData lockData, long refBookId);

	/**
	 * Возвращает данные атрибута справочника по его алиасу
	 * @param refBookId идентификатор справочника
	 * @param attributeAlias алиас аттрибута
	 * @return данные аттрибута
	 */
    RefBookAttribute getAttributeByAlias(long refBookId, String attributeAlias);
}
