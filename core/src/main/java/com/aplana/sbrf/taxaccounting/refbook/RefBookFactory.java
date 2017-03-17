package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;
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
     * Название задачи для справочника
     * @param reportType
     * @param refBookId
     * @param specificReportType
     * @return
     */
    String getTaskName(ReportType reportType, Long refBookId, String specificReportType);

    /**
     * Подробное описание задачи
     * @param reportType
     * @param refBookId
     * @param version
     * @param filter
     * @param specificReportType
     * @return
     */
    String getTaskFullName(ReportType reportType, Long refBookId, Date version, String filter, String specificReportType);

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
     * Получение блокировки и её типа для справочника, null если для справочника нет блокировок
     * @param refBookId
     * @return
     */
    Pair<ReportType, LockData> getLockTaskType(long refBookId);

    /**
     * Формирует ключ блокировки для задачи
     * @param refBookId
     * @param reportType
     * @return
     */
    String generateTaskKey(long refBookId, ReportType reportType);
}
