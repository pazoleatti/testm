package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Данные элемента/версии записи справочника
 * @author dloshkarev
 */
public class RefBookRecord implements Serializable {
    private static final long serialVersionUID = 349999638555117536L;

    /** Значения атрибутов записи */
    private Map<String, RefBookValue> values;

    /** Уникальный идентификатор версии записи справочника. Используется при редактировании */
    private Long uniqueRecordId;

    /** Идентификатор записи без учета версий. Идентификатор группы записей
     * Может быть null - тогда создается новый элемент справочника, а не новая версия элемента */
    private Long recordId;

    /** Дата окончания. Может быть уникальной для каждой записи в группе, в зависиомти от наличия следующей версии */
    private Date versionTo;

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

    public Long getUniqueRecordId() {
        return uniqueRecordId;
    }

    public void setUniqueRecordId(Long uniqueRecordId) {
        this.uniqueRecordId = uniqueRecordId;
    }

    public Date getVersionTo() {
        return versionTo;
    }

    public void setVersionTo(Date versionTo) {
        this.versionTo = versionTo;
    }

    @Override
    public String toString() {
        return "RefBookRecord{" +
                "values=" + values +
                ", uniqueRecordId=" + uniqueRecordId +
                ", recordId=" + recordId +
                ", versionTo=" + versionTo +
                '}';
    }
}
