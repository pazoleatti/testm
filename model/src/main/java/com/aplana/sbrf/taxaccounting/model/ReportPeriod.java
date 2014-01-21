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
	
	private Integer id;
	private String name;

	@Deprecated
	private int months;
	private int order;
	private TaxPeriod taxPeriod;
	private Date startDate;
	private Date endDate;
	
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
	@Deprecated
	public int getMonths() {
		return months;
	}
	
	/**
	 * Задать количество месяцев в периоде
	 * @param months количество месяцев
	 */
	@Deprecated
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
	
	/**
	 * @return
	 * 
	 *
	 */
	public TaxPeriod getTaxPeriod() {
		return taxPeriod;
	}
	public void setTaxPeriod(TaxPeriod taxPeriod) {
		this.taxPeriod = taxPeriod;
	}
	
	
	@SuppressWarnings("deprecation")
	public int getYear(){
		// TODO (sgoryachkin) : Временное решение. Когда налоговый период будет упразднен то эта сущьность будет сохраняться в БД
		// И получаться из БД. http://jira.aplana.com/browse/SBRFACCTAX-4162
		//http://stackoverflow.com/questions/7009655/how-to-use-java-util-calendar-in-gwt
		// Эти вычисления работают правильно только до 3344 года.
//		long milisPerYear = new BigInteger("31536000000").longValue();
		return getTaxPeriod().getYear();
	}
	
	@SuppressWarnings("deprecation")
	public TaxType getTaxType(){
		return getTaxPeriod().getTaxType();
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
}
