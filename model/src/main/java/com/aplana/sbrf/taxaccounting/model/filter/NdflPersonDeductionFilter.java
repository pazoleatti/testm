package com.aplana.sbrf.taxaccounting.model.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Модель для параметров Фильтра вкладки "Сведения о вычетах" страницу РНУ НДФЛ
 */
@Getter
@Setter
@ToString
public class NdflPersonDeductionFilter {
    /**
     * id формы
     */
    private long declarationDataId;
    /**
     * ИНП
     */
    private String inp;
    /**
     * id операции
     */
    private String operationId;
    /**
     * Код вычета
     */
    private String deductionCode;
    /**
     * Код дохода
     */
    private String incomeCode;
    /**
     * Дата начисления дохода с
     */
    private Date calculationDateFrom;
    /**
     * Дата начисления дохода по
     */
    private Date calculationDateTo;
    /**
     * Дата текущего вычета с
     */
    private Date deductionDateFrom;
    /**
     * Дата текущего вычета по
     */
    private Date deductionDateTo;
}
