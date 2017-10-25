package com.aplana.sbrf.taxaccounting.model;

import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * Отчётный период.
 * В нормальной ситуации может быть только один активный отчётный период по каждому виду налога
 * исключения возможны в случае использования корректирующих периодов.
 * @author dsultanbekov
 */
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
	private LocalDateTime startDate;
	/** Дата окончания отчетного периода */
	private LocalDateTime endDate;
	/** Календарная дата начала отчетного периода (квартала) */
	private LocalDateTime calendarStartDate;
	/** Ссылка на федеральный справочника для классификации отчетных периодов */
	private long dictTaxPeriodId;
	private long permissions;

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

	public long getDictTaxPeriodId() {
		return dictTaxPeriodId;
	}

	public void setDictTaxPeriodId(long dictTaxPeriodId) {
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
	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 * Возвращает последнюю дату в отчетном периоде. Это может быть 31 марта, 31 декабря и т.д.
	 * @return
	 */
	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public LocalDateTime getCalendarStartDate() {
		return calendarStartDate;
	}

	public void setCalendarStartDate(LocalDateTime calendarStartDate) {
		this.calendarStartDate = calendarStartDate;
	}

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

	@Override
	public long getPermissions() {
		return permissions;
	}

	@Override
	public void setPermissions(long permissions) {
		this.permissions = permissions;
	}
}
