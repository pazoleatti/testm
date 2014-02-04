package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.Map;

/**
 * Данные элемента/версии записи справочника
 * @author dloshkarev
 */
public class RefBookRecord implements Serializable {
    private static final long serialVersionUID = 349999638555117536L;

    /** Значения атрибутов записи */
    private Map<String, RefBookValue> values;

    /** Идентификатор записи без учета версий.
     * Может быть null - тогда создается новый элемент справочника, а не новая версия элемента */
    private Long recordId;

    public Map<String, RefBookValue> getValues() {
        return values;
    }

    public void setValues(Map<String, RefBookValue> values) {
        this.values = values;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    @Override
    public String toString() {
        return "RefBookRecord{" +
                "values=" + values +
                ", recordId=" + recordId +
                '}';
    }
}
