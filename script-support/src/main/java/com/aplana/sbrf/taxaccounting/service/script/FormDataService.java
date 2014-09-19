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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO для работы с данными по налоговым формам для скриптов 
 * @author auldanov
 */
@ScriptExposed
public interface FormDataService {

    String EDITABLE_CELL_STYLE = "Редактируемая";
    String AUTO_FILL_CELL_STYLE = "Автозаполняемая";

    /**
     * Поиск налоговой формы
     * @deprecated Неактуально с появлением корректирующих периодов
     */
    @Deprecated
	FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId);

    /**
     * Поиск ежемесячной налоговой формы
     * @deprecated Неактуально с появлением корректирующих периодов
     */
    @Deprecated
    FormData findMonth(int formTypeId, FormDataKind kind, int departmentId, int taxPeriodId, int periodOrder);

    /**
     * НФ по отчетному периоду подразделения
     */
    FormData find(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder);

    /**
     * НФ созданная в последнем отчетном периоде подразделения
     */
    @SuppressWarnings("unused")
    FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder);

    /**
     * Посредник для работы со строками налоговой формы во временном и постоянном срезах
     */
	DataRowHelper getDataRowHelper(FormData formData);

    /**
     * Заполнение кэша значений справочника
     * @param formDataId
     * @param refBookCache
     */
    @SuppressWarnings("unused")
    void fillRefBookCache(Long formDataId, Map<Long, Map<String, RefBookValue>> refBookCache);

    /**
     * Консолидация формы
     * Поиск источников и простое объединение строк
     */
    @SuppressWarnings("unused")
    void consolidationSimple(FormData formData, Logger logger);

    /**
     * Консолидация формы с итоговыми строками
     */
    @SuppressWarnings("unused")
    void consolidationTotal(FormData formData, Logger logger, List<String> totalAliases);

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
    RefBookDataProvider getRefBookProvider(RefBookFactory refBookFactory, Long refBookId,
                                           Map<Long, RefBookDataProvider> providerCache);

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
     * Получение Id записи справочника при импорте
     * @param refBookId Id справочника
     * @param recordCache Кэш записей
     * @param providerCache Кэш провайдеров справочников
     * @param refBookCache Кэш записей справочников
     * @param alias Искомый атрибут справочника
     * @param value Искомое значение справочника
     * @param date Дата
     * @param rowIndex Строка из файла для сообщения об ошибке
     * @param colIndex Колонка из файла для сообщения об ошибке
     * @param logger Логгер
     * @param required Фатальность
     * @return
     */
    @SuppressWarnings("unused")
    Map<String, RefBookValue> getRefBookRecordImport(Long refBookId,
                                  Map<Long, Map<String, Long>> recordCache,
                                  Map<Long, RefBookDataProvider> providerCache,
                                  Map<Long, Map<String, RefBookValue>> refBookCache,
                                  String alias, String value, Date date,
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
    @SuppressWarnings("unused")
    Map<String, RefBookValue> getRefBookValue(long refBookId, Long recordId,
                                              Map<Long, Map<String, RefBookValue>> refBookCache);

    /**
     * Получение формы за предыдущий отчетный период.
     * Если форма ежемесячная, то предыдущая форма - это форма за предыдущий месяц (возможно и в другом отчетном периоде).
     * Если несколько отчетных периодов подразделений для найденного отчетного периода, то выбирается последний с формой.
     */
    FormData getFormDataPrev(FormData formData);

    /**
     * Получение номера последней строки в форме за предыдущий отчетный период
     * Если указанная форма первая в году или предыдущих форм нет, то результат будет 0
     */
    @SuppressWarnings("unused")
    BigDecimal getPrevRowNumber(FormData formData, String alias);

    /**
     * Проверка наличия принятой формы за предыдущий период.
     * Если форма без строк, то считается отсутствующей.
     */
    @SuppressWarnings("unused")
    boolean existAcceptedFormDataPrev(FormData formData);

    /**
     * Получение записи справочника
     */
    Map<String, RefBookValue> getRefBookRecord(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                               Map<Long, RefBookDataProvider> providerCache,
                                               Map<Long, Map<String, RefBookValue>> refBookCache,
                                               String alias, String value, Date date,
                                               int rowIndex, String columnName, Logger logger, boolean required);

    /**
     * Сравнить зависимое поле с ожидаемым.
     *
     * @param referenceValue значение зависомого поля (ва загружаемом файле)
     * @param expectedValue ожидаемое значение (в БД)
     * @param rowIndex номер строки в транспортном файле
     * @param colIndex номер колонки в транспортном файле
     * @param logger для вывода сообщений
     * @param required фатальность/обязательность
     */
    void checkReferenceValue(Long refBookId, String referenceValue, String expectedValue, int rowIndex, int colIndex,
                             Logger logger, boolean required);

    /**
     * Проверить существование и принятость квартальной формы, а также наличие данных в ней.
     *
     * @param formTypeId идентификатор типа формы
     * @param kind вид формы
     * @param departmentId идентификатор подразделения
     * @param currentReportPeriodId идентификатор текущего периода
     * @param prevPeriod проверять в предыдущем периоде
     * @param logger логгер
     * @param required фатальность
     */
    @SuppressWarnings("unused")
    void checkFormExistAndAccepted(int formTypeId, FormDataKind kind, int departmentId,
                                          int currentReportPeriodId, Boolean prevPeriod,
                                          Logger logger, boolean required);

    /**
     * Проверить существование и принятость ежемесячной формы, а также наличие данных в ней.
     *
     * @param formTypeId идентификатор типа формы
     * @param kind вид формы
     * @param departmentId идентификатор подразделения
     * @param currentReportPeriodId идентификатор текущего периода
     * @param currentPeriodOrder очередность налоговой формы в рамках налогового периода
     * @param prevPeriod проверять в предыдущем периоде
     * @param logger логгер
     * @param required фатальность
     */
    @SuppressWarnings("unused")
    void checkMonthlyFormExistAndAccepted(int formTypeId, FormDataKind kind, int departmentId,
                                                 int currentReportPeriodId, int currentPeriodOrder, boolean prevPeriod,
                                                 Logger logger, boolean required);

    /**
     * Проверка формы на уникальность с аналогичными параметрам
     */
    @SuppressWarnings("unused")
    boolean checkUnique(FormData formData, Logger logger);
}