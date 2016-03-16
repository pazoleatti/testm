package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Универсальный помощник для работы со справочниками
 * @author avanteev
 */
public interface RefBookHelper {

    /** Режим проверки ссылочных значений */
    enum CHECK_REFERENCES_MODE {
        REFBOOK,                //Справочники
        DEPARTMENT_CONFIG       //Настройки подразделений
    }

    /**
     * Проверка корректности справочных ссылок для настроек подразделений. Выкидывает исключение в зависимости от режима работы
     * http://conf.aplana.com/pages/viewpage.action?pageId=23245326
     * @param refBook справочник, в котором хранятся настройки подразделений
     * @param references список ссылок на справочники в привязке к строкам/полям настроек подразделений/справочников
     * @param mode Режим проверки ссылочных значений
     * @param logger логгер для вывода информации о проверке в лог-панель
     */
    void checkReferenceValues(RefBook refBook, Map<RefBookDataProvider, List<RefBookLinkModel>> references,
                              CHECK_REFERENCES_MODE mode, Logger logger);

    /**
     * Проверка наличия справочных значений
     * @param dataRows
     * @param columns
     */
    void dataRowsCheck(Collection<DataRow<Cell>> dataRows, List<Column> columns);

	/**
	 * Разыменовывание справочных значений
	 *
	 * @param logger
	 * @param dataRows данные для разыменовывания
	 * @param columns список граф для разыменовывания. Зависимые графы должны обязательно иметь родителей в этом списке.
	 */
	void dataRowsDereference(Logger logger, Collection<DataRow<Cell>> dataRows, List<Column> columns);

//	Map<String, String> singleRecordDereference(RefBook refBook, RefBookDataProvider provider,
//			List<RefBookAttribute> attributes, Map<String, RefBookValue> record);

    /**
     * Получить спискок атрибутов второго уровня (для отображения в связной ячейке) для списка атрибутов справочника
     * @param attributes список атрибутов
     * @return список соответсвий атрубут к списку атрибутов(потому что один атрибут может быть в разных колонках)
     */
    Map<Long, List<Long>> getAttrToListAttrId2Map(List<RefBookAttribute> attributes);

    /**
     * Получить кэш списока провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
     * @see RefBookHelper#getAttrToListAttrId2Map
     *
     * @param attributes атрибуты справочника
     * @param attrToListAttrId2Map кэш списка дополнительных атрибутов
     *                             если есть для каждого аттрибута
     *                             @see RefBookHelper#getAttrToListAttrId2Map
     * @return мап провайдера для атрибутов-ссылок
     */
    Map<Long, RefBookDataProvider> getHashedProviders(List<RefBookAttribute> attributes, Map<Long, List<Long>> attrToListAttrId2Map);

//    /**
//     * Получить список соотвествий идентификатора к разыменованному значению записи справочника
//     * В том же списке разименованные значения ссылочных атрибутов полученные по атрибуту второго уровня
//     * @param refBook справоник
//     * @param provider продайдер спраовчнка
//     * @param attributes список атрибутов видимых колонок
//     * @param record запись в справочнике
//     * @return список соответсвий
//     */
//    Map<Long, String> singleRecordDereferenceWithAttrId2(RefBook refBook, RefBookDataProvider provider,
//                                                         List<RefBookAttribute> attributes, Map<String, RefBookValue> record);

    /**
     * Сохраняет настройки подразделений с табличной частью
     * @param uniqueRecordId уникальный идентификатор основной части настроек
     * @param refBookId идентификатор справочника для основной части настроек
     * @param slaveRefBookId идентификатор справочника для табличной части настроек
     * @param reportPeriodId идентификатор периода
     * @param departmentAlias алиас подразделения
     * @param departmentId идентификатор подразделения
     * @param mainConfig данные основной части настроек
     * @param tablePart данные табличной части настроек
     * @param logger логгер
     * @return обновленная информация о настройках подразделений
     */
    RefBookRecordVersion saveOrUpdateDepartmentConfig(Long uniqueRecordId, long refBookId, long slaveRefBookId,
                                                      int reportPeriodId, String departmentAlias, long departmentId,
                                                      Map<String, RefBookValue> mainConfig,
                                                      List<Map<String, RefBookValue>> tablePart,
                                                      Logger logger);

    /**
     * Разыменовывание ссылок, возвращает мапу: attrId: Map<referenceId, value>
     *     attrId - ид атрибута текущего спровочника
     *     referenceId - ссылка(из текущего справочника)
     *     value - значение
     * @param refBook
     * @param refBookPage
     * @return
     */
    Map<Long, Map<Long, String>> dereferenceValues(RefBook refBook, List<Map<String, RefBookValue>> refBookPage, boolean includeAttrId2);

    /**
     * Разыменовывание ссылок, возвращает мапу: attrId: Pair<refBookAttribute, Map<referenceId, refBookValue>>
     *     attrId - ид атрибута текущего спровочника
     *     refBookAttribute - атрибут справочника на который ссылаются
     *     referenceId - ссылка(из текущего справочника)
     *     refBookValue - значение
     * @param refBook
     * @param refBookPage
     * @return
     */
    Map<Long, Pair<RefBookAttribute, Map<Long, RefBookValue>>> dereferenceValuesAttributes(RefBook refBook, List<Map<String, RefBookValue>> refBookPage);
}
