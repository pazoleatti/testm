package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Содержит и переносит данные об отчетном периоде
 */
@Getter
@Setter
public class ReportPeriodResult {

    /** Уникальный идентификатор отчетного периода */
    private Integer id;
    /** Текстовое обозначение отчетного периода */
    private String name;
    /** Ссылка на налоговый период */
    private TaxPeriod taxPeriod;
    /** Дата начала отчетного периода */
    private Date startDate;
    /** Дата окончания отчетного периода */
    private Date endDate;
    /** Календарная дата начала отчетного периода (квартала) */
    private Date calendarStartDate;
    /** Ссылка на федеральный справочника для классификации отчетных периодов */
    private long dictTaxPeriodId;
    /** Ссылка на вид налоговой формы в отчетном периоде */
    private Integer reportPeriodTaxFormTypeId;

    private Date correctionDate;
}
