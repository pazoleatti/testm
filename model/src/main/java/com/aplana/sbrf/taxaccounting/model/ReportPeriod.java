package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Отчётный период.
 * В нормальной ситуации может быть только один активный отчётный период по каждому виду налога
 * исключения возможны в случае использования корректирующих периодов.
 */
@Getter
@Setter
@ToString
public class ReportPeriod implements Serializable, SecuredEntity {
	private static final long serialVersionUID = 1L;

	/** Уникальный идентификатор отчетного периода */
	private Integer id;
	/** Текстовое обозначение отчетного периода */
	private String name;
    /** Текстовое обозначение отчетного периода для нф с нарастающим итогом*/
    private String accName;
    /** Порядок следования отчетного периода в рамках налового */
    private int order;
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

	private long permissions;
}
