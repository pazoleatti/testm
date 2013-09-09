package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * Налоговый период.
 * 
 * Налоговый период может содержать один или несколько {@link ReportPeriod отчётных периодов} 
 * @author dsultanbekov
 */
public class TaxPeriod extends IdentityObject<Integer> {
	private static final long serialVersionUID = 1L;
	
	private TaxType taxType;
	private Date startDate;
	private Date endDate;

	
	/**
	 * @return вид налога
	 * @deprecated Налоговый период скоро не будет существовать. 
	 * Для типа налога отчетного периода используйте <code>ReportPeriod.getTaxType()</code>
	 * 
	 */
	@Deprecated
	public TaxType getTaxType() {
		return taxType;
	}
	
	/**
	 * Задать вид налога
	 * @param taxType вид налога
	 */
	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
	
	/**
	 * @return дата начала периода (включительно)
	 * 
	 * @deprecated Налоговый период скоро не будет существовать. 
	 * Для получения года отчетного периода используйте <code>ReportPeriod.getYear()</code>
	 */
	@Deprecated
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * Задать дату начала периода (включительно)
	 * @param startDate дата начала периода
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * @return дата окончания периода (включительно)
	 * 
	 * @deprecated Налоговый период скоро не будет существовать. 
	 * Для получения года отчетного периода используйте <code>ReportPeriod.getYear()</code>
	 * 
	 */
	@Deprecated
	public Date getEndDate() {
		return endDate;
	}
	
	/**
	 * Задать дату окончания периода (включительно)
	 * @param endDate дата окончания периода
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
