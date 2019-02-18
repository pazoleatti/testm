package com.aplana.sbrf.taxaccounting.model.filter;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * Модель для параметров Фильтра вкладки "Сведения о вычетах" страницу РНУ НДФЛ
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class NdflPersonDeductionFilter {
    /**
     * Общий фильтр
     */
    @ToString.Exclude
    private NdflFilter ndflFilter;
    /**
     * id операции
     */
    private String operationId;
    /**
     * Код вычета
     */
    private String deductionCode;
    /**
     * Дата применения вычета с
     */
    private Date periodPrevDateFrom;
    /**
     * Дата применения вычета по
     */
    private Date periodPrevDateTo;
    /**
     * Дата текущего вычета с
     */
    private Date deductionDateFrom;
    /**
     * Дата текущего вычета по
     */
    private Date deductionDateTo;

    /**
     * Тип подтв. документа
     */
    private String notifType;
    /**
     * Номер подтв. документа
     */
    private String notifNum;
    /**
     * Код источника подтв. документа
     */
    private String notifSource;
    /**
     * Дата подтв. документа с
     */
    private Date notifDateFrom;
    /**
     * Дата подтв. документа по
     */
    private Date notifDateTo;

    /**
     * Номер строки
     */
    private String rowNum;
    /**
     * Идентификатор строки
     */
    private String id;
    /**
     * Дата редактирования с
     */
    private Date modifiedDateFrom;
    /**
     * Дата редактирования по
     */
    private Date modifiedDateTo;
    /**
     * Обновил
     */
    private String modifiedBy;

    public NdflPersonDeductionFilter(NdflFilter ndflFilter) {
        this.ndflFilter = ndflFilter;
    }

    public List<RefBookAsnu> getAsnu() {
        return ndflFilter.getIncome().getAsnu();
    }
}
