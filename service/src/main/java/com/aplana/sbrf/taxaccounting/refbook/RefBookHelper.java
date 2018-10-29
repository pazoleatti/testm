package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookLinkModel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.List;
import java.util.Map;

/**
 * Универсальный помощник для работы со справочниками
 *
 * @author avanteev
 */
public interface RefBookHelper {

    /**
     * Проверка корректности справочных ссылок. Выкидывает исключение в зависимости от режима работы
     * http://conf.aplana.com/pages/viewpage.action?pageId=23245326
     *
     * @param refBook    справочник
     * @param references список ссылок на справочники в привязке к строкам/полям справочников
     * @param logger     логгер для вывода информации о проверке
     */
    void checkReferenceValues(RefBook refBook, Map<RefBookDataProvider, List<RefBookLinkModel>> references, Logger logger);

    /**
     * Разыменовывание ссылок, возвращает мапу: attrId: Map<referenceId, value>
     * attrId - ид атрибута текущего спровочника
     * referenceId - ссылка(из текущего справочника)
     * value - значение
     *
     * @param refBook     справочник
     * @param refBookPage страница справочника
     * @return мапа: attrId: Map<referenceId, value>
     */
    Map<Long, Map<Long, String>> dereferenceValues(RefBook refBook, List<Map<String, RefBookValue>> refBookPage);

    /**
     * Преобразует запись справочника в строку для вывода на экран. При этом ссылочные атрибуты
     * разыменовываются, даты форматируются и т.д.
     *
     * @param refBook справочник
     * @param record  запись справочника
     * @return текстовое представление строки справочника
     */
    String refBookRecordToString(RefBook refBook, RefBookRecord record);
}