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

    /**
     * Уникальный идентификатор, характеризующий элемент СведОпер из ТФ xml. Используется для "связывания" относящихся к этому СведОпер строк разделов 2, 3, 4.
     */
    protected BigDecimal operInfoId;

    /**
     * Доход.Источник выплаты.ОКТМО (Графа 8)
     */
    protected String kpp;

    /**
     * Доход.Источник выплаты.КПП (Графа 9)
     */
    protected String oktmo;

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

    public BigDecimal getOperInfoId() {
        return operInfoId;
    }

    public void setOperInfoId(BigDecimal operInfoId) {
        this.operInfoId = operInfoId;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
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
