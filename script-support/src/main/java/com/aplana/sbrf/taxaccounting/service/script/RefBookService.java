package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormLink;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ScriptExposed
public interface RefBookService {

    /**
     * Запись справочника по Id
     */
    Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);

    /**
     * Строковое значение атрибута записи справочника
     */
    String getStringValue(Long refBookId, Long recordId, String alias);

    /**
     * Числовое значение атрибута записи справочника
     */
    Number getNumberValue(Long refBookId, Long recordId, String alias);

    /**
     * Датированное значение атрибута записи справочника
     */
    Date getDateValue(Long refBookId, Long recordId, String alias);

    /**
     * Разыменование строк НФ
     */
    @SuppressWarnings("unused")
    void dataRowsDereference(Logger logger, Collection<DataRow<Cell>> dataRows, List<Column> columns);
    /**
     * Выполняет указанную логику в новой транзакции
     * @param logic код выполняемый в транзакции
     */
    void executeInNewTransaction(TransactionLogic logic);

    /**
     * Выполняет указанную логику в новой транзакции. Вовращает результат
     * @param logic код выполняемый в транзакции
     */
    <T> T returnInNewTransaction(TransactionLogic<T> logic);

    /**
     * Поиск среди всех элементов справочника (без учета версий) значений уникальных атрибутов, которые бы дублировались с новыми,
     * отдельных справочников.
     * Обеспечение соблюдения уникальности атрибутов в пределах справочника
     * @param recordId уникальный идентификатор записи
     * @param attributes атрибуты справочника
     * @param records новые значения полей элемента справочника
     * @return список пар идентификатор записи-имя атрибута, у которых совпали значения уникальных атрибутов
     */
    List<Pair<String,String>> getMatchedRecordsByUniqueAttributes(Long recordId, List<RefBookAttribute> attributes, List<RefBookRecord> records);

    /**
     * Возвращает данные о версии следующей за указанной
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    RefBookRecordVersion getNextVersion(Long refBookId, Long recordId, Date versionFrom);

    /**
     * Поиск существующих версий, которые могут пересекаться с новой версией
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата окончания актуальности новой версии
     * @param excludedRecordId идентификатор версии записи справочника, которая исключается из проверки пересечения. Используется только при редактировании
     * @return результат проверки по каждой версии, с которой есть пересечение
     */
    List<CheckCrossVersionsResult> checkCrossVersions(Long refBookId, Long recordId,
                                                      Date versionFrom, Date versionTo,
                                                      Long excludedRecordId);

    /**
     * Изменение периода актуальности для указанной версии
     *
     * @param tableName      название таблицы
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @param version        новая дата начала актуальности
     */
    void updateVersionRelevancePeriod(String tableName, Long uniqueRecordId, Date version);

    /**
     * Удаляет указанные версии записи из справочника
     *
     * @param uniqueRecordIds список идентификаторов версий записей, которые будут удалены
     */
    void deleteRecordVersions(String tableName, List<Long> uniqueRecordIds);

    /**
     * Проверка использования записи в налоговых формах
     * @param refBookId идентификатор справочника
     * @param uniqueRecordIds список уникальных идентификаторов записей справочника
     * @param versionFrom дата начала периода
     * @param versionTo дата конца периода
     * @param restrictPeriod
     *      false - возвращает ссылки-использования, период которых НЕ пересекается с указанным периодом
     *      true - возвращает ссылки-использования, период которых пересекается с указанным периодом
     *      null - возвращает все ссылки-использования на указанную запись справочника, без учета периода
     * @return результаты проверки. Сообщения об ошибках
     */
    List<FormLink> isVersionUsedInForms(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo,
                                        Boolean restrictPeriod);

    /**
     * Проверка использования записи в справочниках
     * @param refBookId идентификатор справочника
     * @param uniqueRecordIds список уникальных идентификаторов записей справочника
     * @param versionFrom дата начала периода
     * @param versionTo дата конца периода
     * @param restrictPeriod
     *      false - возвращает ссылки-использования, период которых НЕ пересекается с указанным периодом
     *      true - возвращает ссылки-использования, период которых пересекается с указанным периодом
     *      null - возвращает все ссылки-использования на указанную запись справочника, без учета периода
     * @param excludeUseCheck идентификаторы справочников, которые игнорируются при проверке использования
     * @return результаты проверки. Сообщения об ошибках
     */
    List<String> isVersionUsedInRefBooks(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo,
                                         Boolean restrictPeriod, List<Long> excludeUseCheck);

    /**
     * Проверяет есть ли ссылки на запись справочника в настройках подразделений
     * @param uniqueRecordIds список уникальных идентификаторов записей справочника
     * @param versionFrom дата начала периода
     * @param versionTo дата конца периода
     * @param restrictPeriod
     *      false - возвращает ссылки-использования, период которых НЕ пересекается с указанным периодом
     *      true - возвращает ссылки-использования, период которых пересекается с указанным периодом
     *      null - возвращает все ссылки-использования на указанную запись справочника, без учета периода
     * @param excludeUseCheck идентификаторы справочников, которые игнорируются при проверке использования
     */
    List<String> isVersionUsedInDepartmentConfigs(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, Boolean restrictPeriod, List<Long> excludeUseCheck);

    /**
     * Возвращает данные о версии следующей до указанной
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    RefBookRecordVersion getPreviousVersion(Long refBookId, Long recordId, Date versionFrom);

    /**
     * Возвращает уникальный идентификатор записи, удовлетворяющей указанным условиям
     * @param refBookId идентификатор справочника
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param version дата
     * @return уникальный идентификатор записи, удовлетворяющей указанным условиям
     */
    Long findRecord(Long refBookId, Long recordId, Date version);

    /**
     * Возвращает идентификаторы фиктивных версии, являющихся окончанием указанных версии.
     * Без привязки ко входным параметрам, т.к метод используется просто для удаления по id
     * @param uniqueRecordIds идентификаторы версии записи справочника
     * @return идентификаторы фиктивных версии
     */
    List<Long> getRelatedVersions(List<Long> uniqueRecordIds);

    /**
     * Проверяет существование версий записи справочника
     * @param refBookId идентификатор справочника
     * @param recordIds идентификаторы записей справочника без учета версий
     * @param version версия записи справочника
     * @return
     */
    boolean isVersionsExist(Long refBookId, List<Long> recordIds, Date version);

    /**
     * Создает фиктивную запись, являющуюся датой окончания периода актуальности какой то версии
     * @param refBookId код справочника
     * @param recordId идентификатор записи справочника без учета версий
     * @param version версия записи справочника
     */
    void createFakeRecordVersion(Long refBookId, Long recordId, Date version);

    /**
     * Обновляет значения атрибутов у указанной версии
     * @param refBookId код справочника
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param records список значений атрибутов
     */
    void updateRecordVersion(Long refBookId, Long uniqueRecordId, Map<String, RefBookValue> records);
}