package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.impl.AbstractReadOnlyRefBook;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Справочник созданный на основе перечислений
 * в реализации игнорируются: версия, фильтр, сортировка
 *
 * @author auldanov on 05.02.14.
 */
public abstract class AbstractPermanentRefBook extends AbstractReadOnlyRefBook {

    /**
     * Метод возвращает данные для справочника (id записи, карту алиасов и значений)
     * @return Map<recordId, Map<Alias, Value>>
     */
    abstract protected Map<Long, Map<String, String>> getRecords(String filter);

    /**
     * В реализации игнорируются: версия, сортировка
     */
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        // записи справочника
        Map<Long, Map<String, String>> records = getRecords(filter);

        PagingResult<Map<String, RefBookValue>> pagingResult = new PagingResult<Map<String, RefBookValue>>();

        int cnt = 0;

        for (Map.Entry<Long, Map<String, String>> entry: records.entrySet()){
            if (++cnt > pagingParams.getCount()){
                break;
            }

            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
            RefBookValue recordId = new RefBookValue(RefBookAttributeType.NUMBER, entry.getKey());
            map.put("record_id", recordId);

            for (Map.Entry<String, String> attribute: entry.getValue().entrySet()){
                RefBookValue value = new RefBookValue(RefBookAttributeType.STRING, attribute.getValue());
                map.put(attribute.getKey(), value);
            }

            pagingResult.add(map);
        }

        pagingResult.setTotalCount(records.size());

        return pagingResult;
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
        Map<Long, Map<String, String>> records = getRecords(null);

        for(Map.Entry<Long, Map<String, String>> entry: records.entrySet()){
            if (entry.getKey().equals(recordId)){
                RefBookValue rId = new RefBookValue(RefBookAttributeType.NUMBER, entry.getKey());
                map.put("record_id", rId);

                for (Map.Entry<String, String> attribute: entry.getValue().entrySet()){
                    RefBookValue value = new RefBookValue(RefBookAttributeType.STRING, attribute.getValue());
                    map.put(attribute.getKey(), value);
                }

                return map;
            }
        }

        throw new IllegalArgumentException("There does not exist a record with ID = "+recordId);
    }
}
