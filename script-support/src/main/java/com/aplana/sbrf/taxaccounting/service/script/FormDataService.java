package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.*;
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

    String EDITABLE_CELL_STYLE = "Редактируемая";
    String AUTO_FILL_CELL_STYLE = "Автозаполняемая";

    /**
     * Поиск ежемесячной налоговой формы
     * @deprecated Неактуально с появлением корректирующих периодов
     */
    @Deprecated
    FormData findMonth(int formTypeId, FormDataKind kind, int departmentId, int taxPeriodId, int periodOrder);

    /**
     * Поиск НФ по отчетному периоду подразделения (и месяцу)
     * @param formTypeId Вид НФ
     * @param kind Тип НФ
     * @param departmentReportPeriodId Отчетный период подразделения
     * @param periodOrder Порядковый номер (равен номеру месяца, при нумерации с 1) для ежемесячных форм
     * @param comparativePeriodId период сравнения
     * @param accruing период сравнения
     */
    FormData find(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing);

    /**
     * НФ созданная в последнем отчетном периоде подразделения
     */
    @SuppressWarnings("unused")
    FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing);

    /**
     * Получить шаблон нф.
     * НФ созданная в последнем отчетном периоде подразделения.
     *
     * @param formTypeId идентификатор типа формы
     * @param reportPeriodId идентификатор периода
     */
    FormTemplate getFormTemplate(int formTypeId, int reportPeriodId);

    /**
     * Посредник для работы со строками налоговой формы во временном и постоянном срезах
     */
	DataRowHelper getDataRowHelper(FormData formData);

    /**
     * Заполнение кэша значений справочника
     * @param formData
     * @param refBookCache
     */
    @SuppressWarnings("unused")
    void fillRefBookCache(FormData formData, Map<String, Map<String, RefBookValue>> refBookCache);

    /**
     * Консолидация формы
     * Поиск источников и простое объединение строк
     * Без записи в бд (только сохранение в кэш DataRowHelper)
     */
    @SuppressWarnings("unused")
    void consolidationSimple(FormData formData, Logger logger, TAUserInfo userInfo);

    /**
     * Консолидация формы с итоговыми строками
     * Без записи в бд (только сохранение в кэш DataRowHelper)
     */
    @SuppressWarnings("unused")
    void consolidationTotal(FormData formData, Logger logger, TAUserInfo userInfo, List<String> totalAliases);

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
                                  Map<String, Map<String, RefBookValue>> refBookCache,
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
                                              Map<String, Map<String, RefBookValue>> refBookCache);

    /**
     * Получение формы за предыдущий отчетный период.
     * Если форма ежемесячная, то предыдущая форма - это форма за предыдущий месяц (возможно и в другом отчетном периоде).
     * Если несколько отчетных периодов подразделений для найденного отчетного периода, то выбирается последний с формой.
     */
    FormData getFormDataPrev(FormData formData);

    /**
     * Получение записи справочника
     */
    Map<String, RefBookValue> getRefBookRecord(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                               Map<Long, RefBookDataProvider> providerCache,
                                               Map<String, Map<String, RefBookValue>> refBookCache,
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
     * @param comparativePeriodId Период сравнения - ссылка на DepartmentReportPeriod. Может быть null
     * @param accruing Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения)
     */
    @SuppressWarnings("unused")
    void checkFormExistAndAccepted(int formTypeId, FormDataKind kind, int departmentId,
                                          int currentReportPeriodId, Boolean prevPeriod,
                                          Logger logger, boolean required,
                                          Integer comparativePeriodId, boolean accruing);

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
     * @param comparativePeriodId Период сравнения - ссылка на DepartmentReportPeriod. Может быть null
     * @param accruing Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения)
     */
    @SuppressWarnings("unused")
    void checkMonthlyFormExistAndAccepted(int formTypeId, FormDataKind kind, int departmentId,
                                                 int currentReportPeriodId, int currentPeriodOrder, boolean prevPeriod,
                                                 Logger logger, boolean required,
                                                 Integer comparativePeriodId, boolean accruing);

    /**
     * Проверка формы на уникальность с аналогичными параметрам
     */
    @SuppressWarnings("unused")
    boolean checkUnique(FormData formData, Logger logger);

    /**
     * Сохранение данных из кэша в бд при отстутствии критических ошибкок в логере
     */
    @SuppressWarnings("unused")
    void saveCachedDataRows(FormData formData, Logger logger);

    /**
     * Получить выборку пользователей для представления "Список пользователей"
     * @param filter фильтер
     * @return возвращает страницу со списком пользователей
     */
    PagingResult<TAUserView> getUsersByFilter(MembersFilterData filter);

    /**
     * Возвращает список нф-источников для указанной нф (включая несозданные)
     * @param destinationFormData нф-приемник
     * @param light true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные источники
     * @param stateRestriction ограничение по состоянию для созданных экземпляров
     * @return список нф-источников
     */
    List<Relation> getSourcesInfo(FormData destinationFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction,
                                  TAUserInfo userInfo, Logger logger);

    /**
     * Возвращает список нф-приемников для указанной нф (включая несозданные)
     * @param sourceFormData нф-источник
     * @param light true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные приемники
     * @param stateRestriction ограничение по состоянию для созданных экземпляров
     * @return список нф-приемников
     */
    List<Relation> getDestinationsInfo(FormData sourceFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction,
                                       TAUserInfo userInfo, Logger logger);
}