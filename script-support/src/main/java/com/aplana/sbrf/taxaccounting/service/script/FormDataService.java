package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO для работы с данными по налоговым формам для скриптов 
 * @author auldanov
 */
@ScriptExposed
public interface FormDataService {

    public static String EDITABLE_CELL_STYLE = "Редактируемая";
    public static String AUTO_FILL_CELL_STYLE = "Автозаполняемая";

    /**
     * Поиск налоговой формы
     * @param formTypeId Тип формы
     * @param kind Вид формы
     * @param departmentId Подразделение
     * @param reportPeriodId Отчетный период
     * @return
     */
	FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId);

    /**
     * Посредник для работы со строками налоговой формы во временном и постоянном срезах
     * @param fd
     * @return
     */
	DataRowHelper getDataRowHelper(FormData fd);

    /**
     * Заполнение кэша значений справочника
     * @param formDataId
     * @param refBookCache
     */
    void fillRefBookCache(Long formDataId, Map<Long, Map<String, RefBookValue>> refBookCache);

    /**
     * Консолидация формы
     * Поиск источников и простое объединение строк
     * @param formData
     * @param departmentId
     * @param logger
     */
    void consolidationSimple(FormData formData, int departmentId, Logger logger);

    /**
     * Добавление новой строки
     * Если строка выделена - то после нее
     * Иначе - в конец таблицы
     * @param formData
     * @param currentDataRow
     * @param editableColumns Список алиасов колонок, которые должны быть редактируемыми
     * @param autoFillColumns Список алиасов колонок, которые являются автозаполняемыми
     * @return
     */
    DataRow<Cell> addRow(FormData formData, DataRow<Cell> currentDataRow, List<String> editableColumns,
                         List<String> autoFillColumns);

    /**
     * Получение провайдера справочников через кэш
     * @param refBookFactory
     * @param refBookId
     * @param providerCache
     * @return
     */
    RefBookDataProvider getRefBookProvider(RefBookFactory refBookFactory, Long refBookId, Map<Long, RefBookDataProvider> providerCache);

    /**
     * Получение Id записи справочника при импорте
     * @param refBookId Id справочника
     * @param recordCache Кэш записей
     * @param providerCache Кэш провайдеров справочников
     * @param alias Искомый атрибут справочника
     * @param value Искомое значение справочника
     * @param date Дата
     * @param rowIndex Строка из файла для сообщения об ошибке
     * @param colIndex Колонка из файла для сообщения об ошибке
     * @param logger Логгер
     * @param required Фатальность
     * @return
     */
    Long getRefBookRecordIdImport(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                            Map<Long, RefBookDataProvider> providerCache, String alias, String value, Date date,
                            int rowIndex, int colIndex, Logger logger, boolean required);

    /**
     * Получение Id записи справочника
     * @param refBookId Id справочника
     * @param recordCache Кэш записей
     * @param providerCache Кэш провайдеров справочников
     * @param alias Искомый атрибут справочника
     * @param value Искомое значение справочника
     * @param date Дата
     * @param rowIndex Строка для сообщения об ошибке
     * @param columnName Графа для сообщения об ошибке
     * @param logger Логгер
     * @param required Фатальность
     * @return
     */
    Long getRefBookRecordId(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                            Map<Long, RefBookDataProvider> providerCache, String alias, String value, Date date,
                            int rowIndex, String columnName, Logger logger, boolean required);

    /**
     * Получение значения справочника по Id через кэш
     * @param refBookId
     * @param recordId
     * @param refBookCache
     * @return
     */
    Map<String, RefBookValue> getRefBookValue(long refBookId, Long recordId, Map<Long, Map<String, RefBookValue>> refBookCache);
}