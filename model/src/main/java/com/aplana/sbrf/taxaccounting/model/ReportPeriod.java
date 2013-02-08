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
	
	private int id;
	private String name;
	private boolean active;
	private int months;
	private int order;
	private int taxPeriodId;

	/**
	 * Получить идентификатор отчётного периода
	 */
	public int getId() {
		return id;
	}
	/**
	 * Задать идентфикатор отчётного периода
	 */
	public void setId(int id) {
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
	 * Возвращает значение признака активности отчётного перида
	 * @return true - если период активен, false - в противном случае
	 */
	public boolean isActive() {
		return active;
	}
	/**
	 * Задать значение признака активности отчётного периода
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
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
	
	/**
	 * Возвращает идентификатор налогового периода к которому относится данный отчётный
	 * @return идентфикатор налогового периода
	 */
	public int getTaxPeriodId() {
		return taxPeriodId;
	}
	
	/**
	 * Задаёт идентфикатор налогвого периода к которому относится данный отчётный
	 * @param taxPeriodId идентификатор налогового периода
	 */
	public void setTaxPeriodId(int taxPeriodId) {
		this.taxPeriodId = taxPeriodId;
	}
}
