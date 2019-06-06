package com.aplana.sbrf.taxaccounting.model;


import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSimple;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Модель для типа отчтеного периода из справочника
 */
@Getter
@Setter
public class ReportPeriodType extends RefBookSimple<Long> implements Serializable{

    /** Текстовое обозначение отчетного периода */
    private String name;

    /** Дата начала отчетного периода */
    private Date startDate;

    /** Дата окончания отчетного периода */
    private Date endDate;

    /** Календарная дата начала отчетного периода (квартала) */
    private Date calendarStartDate;

    /** Код типа периода*/
    private String code;
}
