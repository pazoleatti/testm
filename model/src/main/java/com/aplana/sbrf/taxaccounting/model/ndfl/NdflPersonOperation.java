package com.aplana.sbrf.taxaccounting.model.ndfl;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.math.BigDecimal;
import java.util.List;


public abstract class NdflPersonOperation extends IdentityObject<Long> {

    /**
     * Ссылка на запись, которая является источником при формировании консолидированной формы
     */
    protected Long sourceId;

    /**
     * Порядковый номер строки
     * TODO: Здесь подразумеваются целые значения, лучше бы использовать BigInteger.
     */
    protected BigDecimal rowNum;

    /**
     * Идентификатор операции
     */
    protected String operationId;

    /**
     * Ссылка на физлицо
     */
    protected Long ndflPersonId;

    /**
     * Наименование АСНУ источника
     */
    protected String asnu;

    /**
     * Ссылка на справочник АСНУ
     */
    protected Long asnuId;

    public Long getNdflPersonId() {
        return ndflPersonId;
    }

    public void setNdflPersonId(Long ndflPersonId) {
        this.ndflPersonId = ndflPersonId;
    }

    public BigDecimal getRowNum() {
        return rowNum;
    }

    public void setRowNum(BigDecimal rowNum) {
        this.rowNum = rowNum;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
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

    public String getAsnu() {
        return asnu;
    }

    public void setAsnu(String asnu) {
        this.asnu = asnu;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    /**
     * Получение минимального номера строки {@link NdflPersonOperation#rowNum}
     *
     * @param operations список операций
     * @param <T>        тип НДФЛ операции
     * @return минимальное значение номера строки {@link NdflPersonOperation#rowNum} или 1
     */
    public static <T extends NdflPersonOperation> BigDecimal getMinRowNum(List<T> operations) {

        BigDecimal minRowNum = !operations.isEmpty() ? operations.get(0).getRowNum() : new BigDecimal(1);

        for (NdflPersonOperation operation : operations) {
            if (minRowNum == null) {
                minRowNum = operation.getRowNum();
            } else {
                if (minRowNum.compareTo(operation.getRowNum()) > 0) {
                    minRowNum = operation.getRowNum();
                }
            }
        }
        return minRowNum;
    }
}
