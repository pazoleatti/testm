package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * Модель, описывающая события в УН, связанные с изменением данных в определенных таблицах, которые должны быть обработаны со стороны НДФЛ
 * @author dloshkarev
 */
public class TaxChangesEvent extends IdentityObject<Long> {

    /**
     * Название таблицы УН, в которой произошли изменения
     */
    private String tableName;

    /**
     * Идентификатор справочника, который был изменен?
     */
    private Long refBookId;

    /**
     * Название операции, которая была выполнена над данными в таблице
     */
    private String operationName;

    /**
     * Идентификатор строки в таблице, с которой произошли изменения
     */
    private Long tableRowId;

    /**
     * Дата события
     */
    private Date logDateTime;

    /**
     * Названия таблиц УН, в которых произошли изменения
     */
    public enum TaxTableNames {
        SEC_USER,
        DEPARTMENT
    }

    /**
     * Названия операций, которые были сделаны
     */
    public enum Operations {
        insert,
        update,
        delete
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Long getTableRowId() {
        return tableRowId;
    }

    public void setTableRowId(Long tableRowId) {
        this.tableRowId = tableRowId;
    }

    public Date getLogDateTime() {
        return logDateTime;
    }

    public void setLogDateTime(Date logDateTime) {
        this.logDateTime = logDateTime;
    }
}
