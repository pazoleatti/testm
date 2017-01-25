package com.aplana.sbrf.taxaccounting.model.ndfl;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * @author Andrey Drunk
 */
public abstract class NdflPersonOperation extends IdentityObject<Long> {

    /**
     * Cсылка на запись которая является источником при формирование консолидированной формы
     */
    protected Long sourceId;

    /**
     * Порядковый номер строки
     */
    protected Integer rowNum;

    /**
     * Идентификатор операции
     */
    protected Long operationId;

    /**
     * Ссылка на физлицо
     */
    protected Long ndflPersonId;

    public Long getNdflPersonId() {
        return ndflPersonId;
    }

    public void setNdflPersonId(Long ndflPersonId) {
        this.ndflPersonId = ndflPersonId;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public abstract String getTableName();

    public abstract String getSeq();

    public abstract String[] getColumns();

    public abstract String[] getFields();
}
