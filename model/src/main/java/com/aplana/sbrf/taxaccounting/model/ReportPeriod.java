package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;


/**
 * Отчётный период.
 * В нормальной ситуации может быть только один активный отчётный период по каждому виду налога
 * исключения возможны в случае использования корректирующих периодов.
 * @author dsultanbekov
 */
public class ReportPeriod implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String name;

	private int months;
	private int order;
	private TaxPeriod taxPeriod;
	
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
	 * Получить количество месяцев в пероде
	 * @return количество месяцев в периоде
	 */
	public int getMonths() {
		return months;
	}
	
	/**
	 * Задать количество месяцев в периоде
	 * @param months количество месяцев
	 */
	public void setMonths(int months) {
		this.months = months;
	}
	
	/**
	 * Получить порядковый номер отчётного периода в налоговом (начиная с 1)
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
}
