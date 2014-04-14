package com.aplana.sbrf.taxaccounting.refbook;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Универсальный помощник для работы со справочниками
 * @author avanteev
 */
public interface RefBookHelper {

	void dataRowsDereference(Collection<DataRow<Cell>> dataRows,
			List<Column> columns);

	Map<String, String> singleRecordDereference(RefBook refBook, RefBookDataProvider provider,
			List<RefBookAttribute> attributes, Map<String, RefBookValue> record);

    /**
     * Получить спискок атрибутов второго уровня (для отображения в связной ячейке) для списка атрибутов справочника
     * @param attributes список атрибутов
     * @return список соответсвий атрубут к списку атрибутов(потому что один атрибут может быть в разных колонках)
     */
    Map<Long, List<Long>> getAttrToListAttrId2Map(List<RefBookAttribute> attributes);

    /**
     * Получить список соотвествий идентификатора к разименованному значению записи справочника
     * В том же списке разименованные значения ссылочных атрибутов полученные по атрибуту второго уровня
     * @param refBook справоник
     * @param provider продайдер спраовчнка
     * @param attributes список атрибутов видимых колонок
     * @param record запись в справочнике
     * @return список соответсвий
     */
    Map<Long, String> singleRecordDereferenceWithAttrId2(RefBook refBook, RefBookDataProvider provider,
                                                         List<RefBookAttribute> attributes, Map<String, RefBookValue> record);
}
