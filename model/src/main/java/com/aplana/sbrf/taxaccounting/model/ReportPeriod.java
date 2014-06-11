package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Отчётный период.
 * В нормальной ситуации может быть только один активный отчётный период по каждому виду налога
 * исключения возможны в случае использования корректирующих периодов.
 * @author dsultanbekov
 */
public class ReportPeriod implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Уникальный идентификатор отчетного периода */
	private Integer id;
	/** Текстовое обозначение отчетного периода */
	private String name;
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
	private int dictTaxPeriodId;

	/**
	 * Получить идентификатор отчётного периода
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * Задать идентфикатор отчётного периода
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * Получить название периода
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * Задать название периода
	 */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * Получить порядковый номер отчётного периода в налоговом
     *
     * @return порядковый номер отчётного периода в налоговом
     */
    public int getOrder() {
        return order;
    }

    /**
     * Задать порядковый номер отчётного периода в налоговом (начиная с 1)
     * @param order порядковый номер отчётного периода в налоговом
     */
    public void setOrder(int order) {
        this.order = order;
	}

	public int getDictTaxPeriodId() {
		return dictTaxPeriodId;
	}

	public void setDictTaxPeriodId(int dictTaxPeriodId) {
		this.dictTaxPeriodId = dictTaxPeriodId;
	}

	public TaxPeriod getTaxPeriod() {
		return taxPeriod;
	}
	public void setTaxPeriod(TaxPeriod taxPeriod) {
		this.taxPeriod = taxPeriod;
	}

	/**
	 * Возвращает начальную дату отчетного периода. Это может быть 1 января, 1 апреля и т.д.
	 * @return
	 */
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * Возвращает последнюю дату в отчетном периоде. Это может быть 31 марта, 31 декабря и т.д.
	 * @return
	 */
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getCalendarStartDate() {
		return calendarStartDate;
	}

	public void setCalendarStartDate(Date calendarStartDate) {
		this.calendarStartDate = calendarStartDate;
	}
}
